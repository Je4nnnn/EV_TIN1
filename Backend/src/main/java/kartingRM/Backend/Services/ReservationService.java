package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.ReservationDetailsEntity;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Repositories.ReservationRepository;
import kartingRM.Backend.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    public List<ReservationEntity> getAllReservations() {
        return reservationRepository.findAll();
    }

    public ReservationEntity getReservationById(Long id) {
        Optional<ReservationEntity> optionalReserve = reservationRepository.findById(id);
        if (optionalReserve.isPresent()) {
            return optionalReserve.get();
        } else {
            throw new RuntimeException("Reserva no encontrada con ID: " + id);
        }
    }

    private void asegurarClientePrincipal(ReservationEntity reserve) {
        Long clienteId = null;

        if (reserve.getCliente() != null && reserve.getCliente().getId() != null) {
            clienteId = reserve.getCliente().getId();
        } else if (reserve.getDetails() != null && !reserve.getDetails().isEmpty()) {
            clienteId = reserve.getDetails().get(0).getUserId();
        }

        if (clienteId == null) {
            throw new RuntimeException("No se pudo determinar el cliente principal de la reserva.");
        }

        Optional<UserEntity> clienteOpt = userRepository.findById(clienteId);

        if (clienteOpt.isEmpty()) {
            throw new RuntimeException("Cliente principal no encontrado con ID: " + clienteId);
        }

        UserEntity cliente = clienteOpt.get();
        reserve.setCliente(cliente);
    }

    public ReservationEntity saveReservation(ReservationEntity reserve) {
        if (reserve.getDetails() == null || reserve.getDetails().isEmpty()) {
            throw new RuntimeException("La reserva debe incluir al menos un detalle.");
        }

        int cantidadPersonas = (reserve.getNumberOfGuests() != null && reserve.getNumberOfGuests() > 0)
                ? reserve.getNumberOfGuests()
                : reserve.getDetails().size();

        reserve.setNumberOfGuests(cantidadPersonas);
        asegurarClientePrincipal(reserve);

        int cumpleanosAplicados = 0;
        int maxCumpleanos = calcularMaxCumpleanos(cantidadPersonas);
        double montoTotalReserva = 0.0;

        long numeroDias = calcularNumeroDias(reserve);

        for (ReservationDetailsEntity detalle : reserve.getDetails()) {
            if (detalle.getUserId() == null) {
                throw new RuntimeException("Cada detalle debe incluir un userId válido.");
            }

            detalle.setReservation(reserve);

            double descuentoCumpleanos = 0.0;

            if (cumpleanosAplicados < maxCumpleanos) {
                Optional<UserEntity> usuarioOpt = userRepository.findById(detalle.getUserId());
                if (usuarioOpt.isPresent()) {
                    UserEntity usuario = usuarioOpt.get();
                    if (usuario.getDateBirthday() != null
                            && esCumpleanos(usuario.getDateBirthday(), reserve.getCheckInDate())) {
                        descuentoCumpleanos = 0.50;
                        cumpleanosAplicados++;
                    }
                }
            }

            double descuentoCliente = userService.obtenerDescuentoPorCategoria(detalle.getUserId());
            double descuentoGrupo = calcularDescuentoGrupo(cantidadPersonas);

            double descuentoFinal = Math.max(descuentoCumpleanos, Math.max(descuentoCliente, descuentoGrupo));
            detalle.setDiscount(descuentoFinal);

            double tarifaBase = calcularTarifaBase(reserve.getRoomType(), reserve.getStayType()) * numeroDias;
            double montoFinal = tarifaBase * (1 - descuentoFinal);

            detalle.setFinalAmount(montoFinal);
            montoTotalReserva += montoFinal;
        }

        reserve.setFinalAmount(montoTotalReserva);

        List<Long> usuariosAActualizar = reserve.getDetails().stream()
                .map(ReservationDetailsEntity::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        ReservationEntity savedReserve = reservationRepository.save(reserve);

        for (Long userId : usuariosAActualizar) {
            try {
                userService.incrementVisitsAndUpdateCategory(userId);
            } catch (Exception e) {
                // No interrumpir el flujo si falla el conteo de visitas
            }
        }

        return savedReserve;
    }

    public boolean esCumpleanos(LocalDate fechaNacimiento, LocalDate fechaCheckIn) {
        if (fechaNacimiento == null || fechaCheckIn == null) {
            return false;
        }

        return fechaNacimiento.getDayOfMonth() == fechaCheckIn.getDayOfMonth()
                && fechaNacimiento.getMonth() == fechaCheckIn.getMonth();
    }

    public long calcularNumeroDias(ReservationEntity reserve) {
        if (reserve.getCheckInDate() == null || reserve.getCheckOutDate() == null) {
            return 1;
        }

        long dias = ChronoUnit.DAYS.between(reserve.getCheckInDate(), reserve.getCheckOutDate());
        return Math.max(dias, 1);
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

    public ReservationEntity updateReservation(Long id, ReservationEntity reserve) {
        ReservationEntity existingReserve = getReservationById(id);
        existingReserve.setCheckInDate(reserve.getCheckInDate());
        existingReserve.setCheckOutDate(reserve.getCheckOutDate());
        existingReserve.setStayType(reserve.getStayType());
        existingReserve.setFinalAmount(reserve.getFinalAmount());
        existingReserve.setNumberOfGuests(reserve.getNumberOfGuests());
        existingReserve.setRoomType(reserve.getRoomType());

        if (reserve.getCliente() != null && reserve.getCliente().getId() != null) {
            Optional<UserEntity> clienteOpt = userRepository.findById(reserve.getCliente().getId());
            if (clienteOpt.isEmpty()) {
                throw new RuntimeException("Cliente no encontrado con ID: " + reserve.getCliente().getId());
            }
            existingReserve.setCliente(clienteOpt.get());
        }

        return reservationRepository.save(existingReserve);
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    public double calcularTarifaBase(String tipoHabitacion, String stayType) {
        if (tipoHabitacion == null) {
            tipoHabitacion = "Simple";
        }
        if (stayType == null) {
            stayType = "Noche";
        }

        double tarifaNoche = switch (tipoHabitacion) {
            case "Simple" -> 50000;
            case "Double" -> 80000;
            case "Suite" -> 150000;
            default -> 50000;
        };

        return switch (stayType) {
            case "Mañana" -> tarifaNoche * 0.60;
            case "Noche" -> tarifaNoche;
            case "Completo" -> tarifaNoche * 1.40;
            default -> tarifaNoche;
        };
    }

    public double calcularDescuentoGrupo(int cantidadPersonas) {
        if (cantidadPersonas >= 11) {
            return 0.30;
        } else if (cantidadPersonas >= 6) {
            return 0.20;
        } else if (cantidadPersonas >= 3) {
            return 0.10;
        } else {
            return 0.0;
        }
    }

    public Map<String, Map<String, Double>> getReporteIngresosPorVueltasOTiempo(LocalDate fechaInicio, LocalDate fechaFin) {
        List<ReservationEntity> reservas = reservationRepository.findAll();
        Map<String, Map<String, Double>> reporte = new LinkedHashMap<>();

        List<String> meses = Arrays.asList(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        List<String> categorias = Arrays.asList("Simple", "Double", "Suite");

        for (String categoria : categorias) {
            Map<String, Double> ingresosPorMes = new LinkedHashMap<>();
            for (String mes : meses) {
                ingresosPorMes.put(mes, 0.0);
            }
            ingresosPorMes.put("TOTAL", 0.0);
            reporte.put(categoria, ingresosPorMes);
        }

        List<ReservationEntity> reservasFiltradas = reservas.stream()
                .filter(reserva -> reserva.getCheckInDate() != null)
                .filter(reserva -> !reserva.getCheckInDate().isBefore(fechaInicio)
                        && !reserva.getCheckInDate().isAfter(fechaFin))
                .collect(Collectors.toList());

        for (ReservationEntity reserva : reservasFiltradas) {
            String categoria = (reserva.getRoomType() != null) ? reserva.getRoomType() : "Simple";
            String mes = meses.get(reserva.getCheckInDate().getMonthValue() - 1);
            double monto = reserva.getFinalAmount() != null ? reserva.getFinalAmount() : 0.0;

            Map<String, Double> ingresosPorMes = reporte.getOrDefault(categoria, reporte.get("Simple"));
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

    public Map<String, Map<String, Double>> getReporteIngresosPorCantidadDePersonas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<ReservationEntity> reservas = reservationRepository.findAll();
        Map<String, Map<String, Double>> reporte = new LinkedHashMap<>();

        List<String> meses = Arrays.asList(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        List<String> rangos = Arrays.asList("1-2 personas", "3-5 personas", "6-10 personas", "11-15 personas");

        for (String rango : rangos) {
            Map<String, Double> ingresosPorMes = new LinkedHashMap<>();
            for (String mes : meses) {
                ingresosPorMes.put(mes, 0.0);
            }
            ingresosPorMes.put("TOTAL", 0.0);
            reporte.put(rango, ingresosPorMes);
        }

        List<ReservationEntity> reservasFiltradas = reservas.stream()
                .filter(reserva -> reserva.getCheckInDate() != null)
                .filter(reserva -> !reserva.getCheckInDate().isBefore(fechaInicio)
                        && !reserva.getCheckInDate().isAfter(fechaFin))
                .collect(Collectors.toList());

        for (ReservationEntity reserva : reservasFiltradas) {
            int cantidadPersonas = (reserva.getDetails() != null) ? reserva.getDetails().size() : 0;
            String rango = getRangoPorCantidadDePersonas(cantidadPersonas);

            if (rango == null) {
                continue;
            }

            String mes = meses.get(reserva.getCheckInDate().getMonthValue() - 1);
            double monto = reserva.getDetails().stream()
                    .mapToDouble(detalle -> detalle.getFinalAmount() != null ? detalle.getFinalAmount() : 0.0)
                    .sum();

            Map<String, Double> ingresosPorMes = reporte.get(rango);
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