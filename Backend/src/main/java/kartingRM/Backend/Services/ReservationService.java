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

    public ReservationEntity saveReservation(ReservationEntity reserve) {
        if (reserve.getDetails() != null) {
            int cantidadPersonas = reserve.getNumberOfGuests();
            int cumpleanosAplicados = 0;
            int maxCumpleanos = calcularMaxCumpleanos(cantidadPersonas);
            double montoTotalReserva = 0.0;

            // Calcular número de noches/días de estadía
            long numeroDias = calcularNumeroDias(reserve);

            for (ReservationDetailsEntity detalle : reserve.getDetails()) {
                detalle.setReservation(reserve);

                double descuentoCumpleanos = 0.0;

                // Verificar si el huésped cumple años durante el check-in
                if (cumpleanosAplicados < maxCumpleanos && detalle.getUserId() != null) {
                    Optional<UserEntity> usuarioOpt = userRepository.findById(detalle.getUserId());
                    if (usuarioOpt.isPresent()) {
                        UserEntity usuario = usuarioOpt.get();
                        if (usuario.getDateBirthday() != null && esCumpleanos(usuario.getDateBirthday(), reserve.getCheckInDate())) {
                            descuentoCumpleanos = 0.50; // 50% descuento de cumpleaños
                            cumpleanosAplicados++;
                        }
                    }
                }

                double descuentoCliente = 0.0;
                if (detalle.getUserId() != null) {
                    descuentoCliente = userService.obtenerDescuentoPorCategoria(detalle.getUserId());
                }
                double descuentoGrupo = calcularDescuentoGrupo(cantidadPersonas);

                // Se aplica el mayor descuento (no se acumulan)
                double descuentoFinal = Math.max(descuentoCumpleanos, Math.max(descuentoCliente, descuentoGrupo));
                detalle.setDiscount(descuentoFinal);

                // Tarifa base por noche/turno del tipo de habitación, multiplicada por el número de días
                double tarifaBase = calcularTarifaBase(reserve.getRoomType(), reserve.getStayType()) * numeroDias;
                double montoFinal = tarifaBase * (1 - descuentoFinal);
                detalle.setFinalAmount(montoFinal);
                montoTotalReserva += montoFinal;
            }

            reserve.setFinalAmount(montoTotalReserva);
        }

        ReservationEntity savedReserve = reservationRepository.save(reserve);
        // Actualizar visitas del cliente principal
        if (savedReserve.getCliente() != null) {
            try {
                userService.incrementVisitsAndUpdateCategory(savedReserve.getCliente().getId());
            } catch (Exception e) {
                // No interrumpir el flujo si falla el conteo de visitas
            }
        }
        return savedReserve;
    }

    /**
     * Verifica si la fecha de cumpleaños coincide (mismo día y mes) con la fecha de check-in.
     */
    public boolean esCumpleanos(LocalDate fechaNacimiento, LocalDate fechaCheckIn) {
        if (fechaNacimiento == null || fechaCheckIn == null) return false;
        return fechaNacimiento.getDayOfMonth() == fechaCheckIn.getDayOfMonth()
                && fechaNacimiento.getMonth() == fechaCheckIn.getMonth();
    }

    /**
     * Calcula el número de noches/días de la reserva.
     * Si el stayType es "Completo" (día y noche), cuenta los días entre checkIn y checkOut.
     * Si es "Mañana" o "Noche", cuenta los turnos.
     */
    public long calcularNumeroDias(ReservationEntity reserve) {
        if (reserve.getCheckInDate() == null || reserve.getCheckOutDate() == null) {
            return 1;
        }
        long dias = ChronoUnit.DAYS.between(reserve.getCheckInDate(), reserve.getCheckOutDate());
        return Math.max(dias, 1); // Mínimo 1
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
        return reservationRepository.save(existingReserve);
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    /**
     * Tarifa base por tipo de habitación y tipo de estancia.
     * stayType: "Mañana" = turno diurno, "Noche" = turno nocturno, "Completo" = día completo
     */
    public double calcularTarifaBase(String tipoHabitacion, String stayType) {
        if (tipoHabitacion == null) tipoHabitacion = "Simple";
        if (stayType == null) stayType = "Noche";

        // Tarifas base por noche (turno completo)
        double tarifaNoche = switch (tipoHabitacion) {
            case "Simple" -> 50000;
            case "Double" -> 80000;
            case "Suite"  -> 150000;
            default       -> 50000;
        };

        // Turno de mañana: 60% de la tarifa noche
        // Turno de noche: 100% de la tarifa
        // Completo (mañana + noche): 140% de la tarifa (descuento del 30% sobre suma de ambos)
        return switch (stayType) {
            case "Mañana"  -> tarifaNoche * 0.60;
            case "Noche"   -> tarifaNoche;
            case "Completo"-> tarifaNoche * 1.40;
            default        -> tarifaNoche;
        };
    }

    public double calcularDescuentoGrupo(int cantidadPersonas) {
        if (cantidadPersonas >= 11) {
            return 0.30; // 30%
        } else if (cantidadPersonas >= 6) {
            return 0.20; // 20%
        } else if (cantidadPersonas >= 3) {
            return 0.10; // 10%
        } else {
            return 0.0;  // 0%
        }
    }

    // Reporte de ingresos por tipo de habitación
    public Map<String, Map<String, Double>> getReporteIngresosPorVueltasOTiempo(LocalDate fechaInicio, LocalDate fechaFin) {
        List<ReservationEntity> reservas = reservationRepository.findAll();

        Map<String, Map<String, Double>> reporte = new LinkedHashMap<>();

        List<String> meses = Arrays.asList("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");
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
                .filter(reserva -> !reserva.getCheckInDate().isBefore(fechaInicio) && !reserva.getCheckInDate().isAfter(fechaFin))
                .collect(Collectors.toList());

        for (ReservationEntity reserva : reservasFiltradas) {
            String categoria = (reserva.getRoomType() != null) ? reserva.getRoomType() : "Simple";

            String mes = meses.get(reserva.getCheckInDate().getMonthValue() - 1);
            double monto = reserva.getFinalAmount();

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

    // Reporte por cantidad de personas
    public Map<String, Map<String, Double>> getReporteIngresosPorCantidadDePersonas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<ReservationEntity> reservas = reservationRepository.findAll();
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

        List<ReservationEntity> reservasFiltradas = reservas.stream()
                .filter(reserva -> reserva.getCheckInDate() != null)
                .filter(reserva -> !reserva.getCheckInDate().isBefore(fechaInicio) && !reserva.getCheckInDate().isAfter(fechaFin))
                .collect(Collectors.toList());

        for (ReservationEntity reserva : reservasFiltradas) {
            int cantidadPersonas = reserva.getDetails().size();
            String rango = getRangoPorCantidadDePersonas(cantidadPersonas);

            if (rango == null) continue;
            String mes = meses.get(reserva.getCheckInDate().getMonthValue() - 1);
            double monto = reserva.getDetails().stream().mapToDouble(ReservationDetailsEntity::getFinalAmount).sum();

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