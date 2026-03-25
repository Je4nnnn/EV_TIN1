package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.ReserveDetailsEntity;
import kartingRM.Backend.Entities.ReserveEntity;
import kartingRM.Backend.Repositories.ReserveRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;

import java.io.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ReserveService {

    @Autowired
    private UserService userService;

    @Autowired
    private ReserveRepository reserveRepository;

    @Autowired
    private JavaMailSender mailSender;

    public List<ReserveEntity> getAllReserves() {
        return reserveRepository.findAll();
    }

    public ReserveEntity getReserveById(Long id) {
        Optional<ReserveEntity> optionalReserve = reserveRepository.findById(id);
        if (optionalReserve.isPresent()) {
            return optionalReserve.get();
        } else {
            throw new RuntimeException("Reserva no encontrada con ID: " + id);
        }
    }

    public byte[] generarComprobantePdf(ReserveEntity reserve) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
    
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
    
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        float yPosition = 750; // posicion eje Y
        float margin = 50; // margen 
        float lineHeight = 15; // altura 
    
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.showText("Comprobante de Reserva");
        contentStream.endText();
    
        yPosition -= lineHeight;
    
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.showText("Código de Reserva: " + reserve.getCodigo_reserva());
        contentStream.newLineAtOffset(0, -lineHeight);
        contentStream.showText("Fecha y Hora de la Reserva: " + reserve.getFecha_uso() + " " + reserve.getHora_inicio() + " - " + reserve.getHora_fin());
        contentStream.newLineAtOffset(0, -lineHeight);
        contentStream.showText("Número de Vueltas o Tiempo Máximo: " + reserve.getVueltas_o_tiempo());
        contentStream.newLineAtOffset(0, -lineHeight);
        contentStream.showText("Cantidad de Personas: " + reserve.getCantidad_personas());
        contentStream.newLineAtOffset(0, -lineHeight);
        contentStream.showText("Nombre del Cliente: " + reserve.getDetalles().get(0).getMemberName());
        contentStream.endText();
    
        yPosition -= (lineHeight * 5);
    
        // detalles tablita
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.showText("Detalle de Pago:");
        contentStream.endText();
    
        yPosition -= lineHeight;
    
        // encabezados
        float tableWidth = 500;
        float[] columnWidths = {100, 80, 80, 80, 80, 80};
        String[] headers = {"Nombre", "Tarifa Base", "Descuento", "Monto Final", "IVA", "Total con IVA"};
    
        double totalReservaConIva = 0.0; 

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        float xPosition = margin;
        for (String header : headers) {
            contentStream.beginText();
            contentStream.newLineAtOffset(xPosition, yPosition);
            contentStream.showText(header);
            contentStream.endText();
            xPosition += columnWidths[headers.length - headers.length + 1];
        }
        yPosition -= lineHeight; 

        contentStream.setFont(PDType1Font.HELVETICA, 10);
        for (ReserveDetailsEntity detalle : reserve.getDetalles()) {
            double tarifaBase = detalle.getMontoFinal() / (1 - detalle.getDiscount());
            double iva = detalle.getMontoFinal() * 0.19; //IVA
            double totalConIva = detalle.getMontoFinal() + iva;

            totalReservaConIva += totalConIva; // Sumar el total con IVA

            xPosition = margin;
            String[] row = {
                detalle.getMemberName(),
                String.format("%.2f", tarifaBase),
                String.format("%.2f", 100 * detalle.getDiscount()) + " %",
                String.format("%.2f", detalle.getMontoFinal()),
                String.format("%.2f", iva),
                String.format("%.2f", totalConIva)
            };

            for (String cell : row) {
                contentStream.beginText();
                contentStream.newLineAtOffset(xPosition, yPosition);
                contentStream.showText(cell);
                contentStream.endText();
                xPosition += columnWidths[row.length - row.length + 1]; 
            }

            yPosition -= lineHeight;

            // para agregar otra pagina
            if (yPosition < 50) {
                contentStream.close();
                page = new PDPage();
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                yPosition = 750; 
            }
        }
        // total de la reserva al final
        yPosition -= lineHeight;
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.showText("Total Reserva: " + "$" + String.format("%.2f", totalReservaConIva));
        contentStream.endText();
    
        contentStream.close();
    
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();
    
        return outputStream.toByteArray();
    }

    public void enviarComprobantePorCorreo(String[] destinatarios, byte[] pdfBytes, String reserveCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
    
        helper.setTo(destinatarios);
        helper.setSubject("Comprobante de Reserva " + reserveCode);
        helper.setText("Hola");
    
        helper.addAttachment("comprobante.pdf", new ByteArrayDataSource(pdfBytes, "application/pdf"));
    
        mailSender.send(message);
    }

    public ReserveEntity saveReserve(ReserveEntity reserve) {
        if (reserve.getDetalles() != null) {
            int cantidadPersonas = reserve.getCantidad_personas();
            int maxCumpleanos = calcularMaxCumpleanos(cantidadPersonas);
            int cumpleanosAplicados = 0;
            double montoTotalReserva = 0.0;
    
            for (ReserveDetailsEntity detalle : reserve.getDetalles()) {
                detalle.setReserve(reserve);
                double descuentoCumpleanos = 0.0;
                if (cumpleanosAplicados < maxCumpleanos &&
                        detalle.getDateBirthday().equals(reserve.getFecha_uso())) {
                    descuentoCumpleanos = 0.50;
                    cumpleanosAplicados++;
                }
    
                double descuentoCliente = userService.obtenerDescuentoPorCategoria(detalle.getUserId());
                double descuentoGrupo = calcularDescuentoGrupo(cantidadPersonas);
    
                double descuentoFinal = Math.max(descuentoCumpleanos, Math.max(descuentoCliente, descuentoGrupo));
                detalle.setDiscount(descuentoFinal);
    
                double tarifaBase = calcularTarifaBase(reserve.getVueltas_o_tiempo());
                double montoFinal = tarifaBase * (1 - descuentoFinal);
                detalle.setMontoFinal(montoFinal);
                montoTotalReserva += montoFinal;
            }
    
            reserve.setMontoFinal(montoTotalReserva);
        }
    
        ReserveEntity savedReserve = reserveRepository.save(reserve);
    
        try {
            // Generar el PDF en memoria
            byte[] pdfBytes = generarComprobantePdf(savedReserve);
    
            // Obtener los correos electrónicos de los usuarios
            String[] emails = savedReserve.getDetalles().stream()
                    .map(detalle -> userService.findUserById(detalle.getUserId()).getEmail())
                    .toArray(String[]::new);
    
            // Enviar el PDF por correo
            enviarComprobantePorCorreo(emails, pdfBytes, savedReserve.getCodigo_reserva());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar o enviar el comprobante.");
        }
    
        return savedReserve;
    }

    public int calcularMaxCumpleanos(int cantidadPersonas) {
        if (cantidadPersonas >= 6 && cantidadPersonas <= 10) {
            return 2; 
        } else if (cantidadPersonas >= 3 && cantidadPersonas <= 5) {
            return 1; 
        } else {
            return 0; 
        }
    }

    public ReserveEntity updateReserve(Long id, ReserveEntity reserve) {
        ReserveEntity existingReserve = getReserveById(id);
        existingReserve.setHora_inicio(reserve.getHora_inicio());
        existingReserve.setMontoFinal(reserve.getMontoFinal());
        existingReserve.setHora_fin(reserve.getHora_fin());
        existingReserve.setCantidad_personas(reserve.getCantidad_personas());
        return reserveRepository.save(existingReserve);
    }

    public void deleteReserve(Long id) {
        reserveRepository.deleteById(id);
    }

    public boolean esFinDeSemana(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
    }

    public boolean esDiaFeriado(LocalDate fecha) {
        Set<LocalDate> diasFeriados = Set.of(
            LocalDate.of(2025, 9, 18), // Fiestas Patrias
            LocalDate.of(2025, 12, 25) // Navidad
        ); //dejo pendiente agregar más feriados
        return diasFeriados.contains(fecha);
    } 

    public double calcularTarifaBase(String vueltasOTiempo) {
        double tarifaBase = switch (vueltasOTiempo) {
            case "10 vueltas", "10 minutos" -> 15000;
            case "15 vueltas", "15 minutos" -> 20000;
            case "20 vueltas", "20 minutos" -> 25000;
            default -> throw new IllegalArgumentException("Vueltas o tiempo no válido");
        };
    
        // Determinar tarifa base y duración máxima
        return tarifaBase;
    }

    
    public double calcularDescuentoGrupo(int cantidadPersonas) {
        if (cantidadPersonas >= 11) {
            return 0.30; // 30%
        } else if (cantidadPersonas >= 6) {
            return 0.20; // 20%
        } else if (cantidadPersonas >= 3) {
            return 0.10; // 10%
        } else {
            return 0.0; // 0%
        }
    }

    //  Reporte por cantidad de vueltas o tiempo
    public Map<String, Map<String, Double>> getReporteIngresosPorVueltasOTiempo(LocalDate fechaInicio, LocalDate fechaFin) {
        List<ReserveEntity> reservas = reserveRepository.findAll(); // todas las reservas
        System.out.println("Reservas totales: " + reservas);
    
        Map<String, Map<String, Double>> reporte = new LinkedHashMap<>();

        List<String> meses = Arrays.asList("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");
        List<String> categorias = Arrays.asList("10 vueltas o máx 10 min", "15 vueltas o máx 15 min", "20 vueltas o máx 20 min");
    
        for (String categoria : categorias) {
            Map<String, Double> ingresosPorMes = new LinkedHashMap<>();
            for (String mes : meses) {
                ingresosPorMes.put(mes, 0.0);
            }
            ingresosPorMes.put("TOTAL", 0.0);
            reporte.put(categoria, ingresosPorMes);
        }
    
        // Filtrar reservas entre las fechas dadas
        List<ReserveEntity> reservasFiltradas = reservas.stream()
                .filter(reserva -> reserva.getFecha_uso() != null)
                .filter(reserva -> !reserva.getFecha_uso().isBefore(fechaInicio) && !reserva.getFecha_uso().isAfter(fechaFin))
                .collect(Collectors.toList());
    
        System.out.println("Reservas filtradas: " + reservasFiltradas);

        for (ReserveEntity reserva : reservasFiltradas) {
            String categoria = switch (reserva.getVueltas_o_tiempo()) {
                case "10 vueltas", "10 minutos" -> "10 vueltas o máx 10 min";
                case "15 vueltas", "15 minutos" -> "15 vueltas o máx 15 min";
                case "20 vueltas", "20 minutos" -> "20 vueltas o máx 20 min";
                default -> "Otros";
            };
    
            String mes = meses.get(reserva.getFecha_uso().getMonthValue() - 1); // nombre del mes
            double monto = reserva.getMontoFinal();
    
            // Sumar ingresos al mes correspondiente
            Map<String, Double> ingresosPorMes = reporte.get(categoria);
            ingresosPorMes.put(mes, ingresosPorMes.get(mes) + monto);
            ingresosPorMes.put("TOTAL", ingresosPorMes.get("TOTAL") + monto);
        }

        Map<String, Double> totalPorMes = new LinkedHashMap<>();
        for (String mes : meses) {
            double totalMes = reporte.values().stream()
                    .mapToDouble(ingresosPorMes -> ingresosPorMes.get(mes))
                    .sum();
            totalPorMes.put(mes, totalMes);
        }
        totalPorMes.put("TOTAL", totalPorMes.values().stream().mapToDouble(Double::doubleValue).sum());
        reporte.put("TOTAL", totalPorMes);
    
        return reporte;
    }

    //  Reporte por cantidad de personas
    public Map<String, Map<String, Double>> getReporteIngresosPorCantidadDePersonas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<ReserveEntity> reservas = reserveRepository.findAll(); // Obtén todas las reservas
        Map<String, Map<String, Double>> reporte = new LinkedHashMap<>();

        List<String> meses = Arrays.asList("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");
        List<String> rangos = Arrays.asList("1-2 personas", "3-5 personas", "6-10 personas", "11-15 personas");
    
        for (String rango : rangos) {
            Map<String, Double> ingresosPorMes = new LinkedHashMap<>();
            for (String mes : meses) {
                ingresosPorMes.put(mes, 0.0);
            }
            ingresosPorMes.put("TOTAL", 0.0);
            reporte.put(rango, ingresosPorMes);
        }

        List<ReserveEntity> reservasFiltradas = reservas.stream()
                .filter(reserva -> reserva.getFecha_uso() != null)
                .filter(reserva -> !reserva.getFecha_uso().isBefore(fechaInicio) && !reserva.getFecha_uso().isAfter(fechaFin))
                .collect(Collectors.toList());
    
        // Procesar las reservas
        for (ReserveEntity reserva : reservasFiltradas) {
            int cantidadPersonas = reserva.getDetalles().size();
            String rango = getRangoPorCantidadDePersonas(cantidadPersonas);
    
            if (rango == null) continue;
            String mes = meses.get(reserva.getFecha_uso().getMonthValue() - 1);
            double monto = reserva.getDetalles().stream().mapToDouble(ReserveDetailsEntity::getMontoFinal).sum();

            Map<String, Double> ingresosPorMes = reporte.get(rango);
            ingresosPorMes.put(mes, ingresosPorMes.get(mes) + monto);
            ingresosPorMes.put("TOTAL", ingresosPorMes.get("TOTAL") + monto);
        }
    
        // Agregar totales por mes
        Map<String, Double> totalPorMes = new LinkedHashMap<>();
        for (String mes : meses) {
            double totalMes = reporte.values().stream()
                    .mapToDouble(ingresosPorMes -> ingresosPorMes.get(mes))
                    .sum();
            totalPorMes.put(mes, totalMes);
        }
        totalPorMes.put("TOTAL", totalPorMes.values().stream().mapToDouble(Double::doubleValue).sum());
        reporte.put("TOTAL", totalPorMes);
    
        return reporte;
    }
    
    public String getRangoPorCantidadDePersonas(int cantidadPersonas) {
        if (cantidadPersonas >= 1 && cantidadPersonas <= 2) {
            return "1-2 personas";
        } else if (cantidadPersonas >= 3 && cantidadPersonas <= 5) {
            return "3-5 personas";
        } else if (cantidadPersonas >= 6 && cantidadPersonas <= 10) {
            return "6-10 personas";
        } else if (cantidadPersonas >= 11 && cantidadPersonas <= 15) {
            return "11-15 personas";
        }
        return null;
    }
}