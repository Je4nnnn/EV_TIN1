package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.ReservationDetailsEntity;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Entities.TouristPackageEntity;
import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.ReservationRepository;
import kartingRM.Backend.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final int MAX_ACTIVE_RESERVATIONS_PER_RUT = 3;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TouristPackageService touristPackageService;

    @Autowired
    private RoomService roomService;

    @Transactional(readOnly = true)
    public List<ReservationEntity> getAllReservations() {
        List<ReservationEntity> reservations = reservationRepository.findByCancelledFalse();
        reservations.forEach(this::initializeReservation);
        return reservations;
    }

    @Transactional(readOnly = true)
    public ReservationEntity getReservationById(Long id) {
        ReservationEntity reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + id));
        initializeReservation(reservation);
        return reservation;
    }

    @Transactional
    public ReservationEntity saveReservation(ReservationEntity reserve) {
        validateReservation(reserve);
        TouristPackageEntity selectedPackage = applyTouristPackageDataIfNeeded(reserve);
        UserEntity mainClient = resolveMainClient(reserve);
        validateActiveReservationLimit(mainClient.getRut(), null);
        reserve.setCliente(mainClient);
        prepareReservationForPersistence(reserve);
        ReservationEntity savedReserve = reservationRepository.save(reserve);
        consumeTouristPackageSlotIfNeeded(selectedPackage);
        updateVisitCounters(savedReserve);
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
        }
        if (cantidadPersonas >= 3 && cantidadPersonas <= 5) {
            return 1;
        }
        return 0;
    }

    @Transactional
    public ReservationEntity updateReservation(Long id, ReservationEntity reserve) {
        ReservationEntity existingReserve = getReservationById(id);
        copyReservationData(existingReserve, reserve);
        validateReservation(existingReserve);
        applyTouristPackageDataIfNeeded(existingReserve);
        UserEntity mainClient = resolveMainClient(existingReserve);
        validateActiveReservationLimit(mainClient.getRut(), id);
        existingReserve.setCliente(mainClient);
        prepareReservationForPersistence(existingReserve);
        return reservationRepository.save(existingReserve);
    }

    public void deleteReservation(Long id) {
        ReservationEntity reservation = getReservationById(id);
        if (reservation.getTouristPackageId() != null) {
            touristPackageService.releasePackageSlot(reservation.getTouristPackageId());
        }
        reservation.setCancelled(true);
        reservation.setCancelledAt(LocalDateTime.now());
        reservationRepository.save(reservation);
    }

    public double calcularTarifaBase(String tipoHabitacion, String stayType) {
        String roomType = (tipoHabitacion == null || tipoHabitacion.isBlank()) ? "Simple" : tipoHabitacion.trim();
        String stayMode = (stayType == null || stayType.isBlank()) ? "Noche" : stayType.trim();

        double tarifaNoche = switch (roomType) {
            case "Simple" -> 50000;
            case "Double" -> 80000;
            case "Suite" -> 150000;
            default -> 50000;
        };

        return switch (stayMode) {
            case "Manana", "Mañana" -> tarifaNoche * 0.60;
            case "Noche" -> tarifaNoche;
            case "Completo" -> tarifaNoche * 1.40;
            default -> tarifaNoche;
        };
    }

    public double calcularDescuentoGrupo(int cantidadPersonas) {
        if (cantidadPersonas >= 11) {
            return 0.30;
        }
        if (cantidadPersonas >= 6) {
            return 0.20;
        }
        if (cantidadPersonas >= 3) {
            return 0.10;
        }
        return 0.0;
    }

    @Transactional(readOnly = true)
    public Map<String, Map<String, Double>> getReporteIngresosPorVueltasOTiempo(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Map<String, Double>> reporte = new LinkedHashMap<>();
        List<String> meses = getMeses();
        List<String> categorias = Arrays.asList("Simple", "Double", "Suite");

        for (String categoria : categorias) {
            reporte.put(categoria, createMonthlyAccumulator(meses));
        }

        for (ReservationEntity reserva : filterReservationsBetween(fechaInicio, fechaFin)) {
            String categoria = reserva.getRoomType() != null ? reserva.getRoomType() : "Simple";
            String mes = meses.get(reserva.getCheckInDate().getMonthValue() - 1);
            double monto = reserva.getFinalAmount() != null ? reserva.getFinalAmount() : 0.0;

            Map<String, Double> ingresosPorMes = reporte.getOrDefault(categoria, reporte.get("Simple"));
            ingresosPorMes.put(mes, ingresosPorMes.get(mes) + monto);
            ingresosPorMes.put("TOTAL", ingresosPorMes.get("TOTAL") + monto);
        }

        reporte.put("TOTAL", calculateTotals(reporte, meses));
        return reporte;
    }

    @Transactional(readOnly = true)
    public Map<String, Map<String, Double>> getReporteIngresosPorCantidadDePersonas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Map<String, Double>> reporte = new LinkedHashMap<>();
        List<String> meses = getMeses();
        List<String> rangos = Arrays.asList("1-2 personas", "3-5 personas", "6-10 personas", "11-15 personas");

        for (String rango : rangos) {
            reporte.put(rango, createMonthlyAccumulator(meses));
        }

        for (ReservationEntity reserva : filterReservationsBetween(fechaInicio, fechaFin)) {
            int cantidadPersonas = reserva.getDetails() != null ? reserva.getDetails().size() : 0;
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

        reporte.put("TOTAL", calculateTotals(reporte, meses));
        return reporte;
    }

    public String getRangoPorCantidadDePersonas(int cantidadPersonas) {
        if (cantidadPersonas >= 1 && cantidadPersonas <= 2) {
            return "1-2 personas";
        }
        if (cantidadPersonas >= 3 && cantidadPersonas <= 5) {
            return "3-5 personas";
        }
        if (cantidadPersonas >= 6 && cantidadPersonas <= 10) {
            return "6-10 personas";
        }
        if (cantidadPersonas >= 11 && cantidadPersonas <= 15) {
            return "11-15 personas";
        }
        return null;
    }

    private void validateReservation(ReservationEntity reserve) {
        boolean reservationFromPackage = reserve.getTouristPackageId() != null;

        if (reserve == null) {
            throw new BusinessException("Debe enviar una reserva valida.");
        }

        if (reserve.getDetails() == null || reserve.getDetails().isEmpty()) {
            throw new BusinessException("La reserva debe incluir al menos un detalle.");
        }

        if (!reservationFromPackage && (reserve.getCheckInDate() == null || reserve.getCheckOutDate() == null)) {
            throw new BusinessException("La reserva debe incluir fecha de check-in y check-out.");
        }

        if (!reservationFromPackage && reserve.getCheckOutDate().isBefore(reserve.getCheckInDate())) {
            throw new BusinessException("La fecha de check-out no puede ser anterior al check-in.");
        }

        if (!reservationFromPackage && (reserve.getStayType() == null || reserve.getStayType().isBlank())) {
            throw new BusinessException("Debe especificar el tipo de estancia.");
        }

        if (!reservationFromPackage && (reserve.getRoomType() == null || reserve.getRoomType().isBlank())) {
            throw new BusinessException("Debe especificar el tipo de habitacion.");
        }

        if (reserve.getRoomId() == null) {
            throw new BusinessException("Debe seleccionar una habitacion disponible.");
        }

        int guestCount = resolveGuestCount(reserve);
        if (guestCount < 1 || guestCount > 15) {
            throw new BusinessException("La cantidad de huespedes debe estar entre 1 y 15.");
        }

        for (ReservationDetailsEntity detail : reserve.getDetails()) {
            if (detail.getUserId() == null) {
                throw new BusinessException("Cada detalle debe incluir un userId valido.");
            }
            if (detail.getGuestName() == null || detail.getGuestName().isBlank()) {
                throw new BusinessException("Cada detalle debe incluir el nombre del huesped.");
            }
        }

        if (reserve.getTouristPackageId() != null
                && (reserve.getTouristPackageName() == null || reserve.getTouristPackageName().isBlank())) {
            reserve.setTouristPackageName("Paquete turistico");
        }
    }

    private void prepareReservationForPersistence(ReservationEntity reserve) {
        if (reserve.getCancelled() == null) {
            reserve.setCancelled(Boolean.FALSE);
        }

        reserve.setNumberOfGuests(resolveGuestCount(reserve));
        reserve.setStayType(reserve.getStayType().trim());
        reserve.setRoomType(reserve.getRoomType().trim());
        assignSelectedRoom(reserve);
        reserve.setDetails(sortReservationDetails(reserve.getDetails()));
        recalculateReservationAmounts(reserve);
    }

    private int resolveGuestCount(ReservationEntity reserve) {
        if (reserve.getNumberOfGuests() != null && reserve.getNumberOfGuests() > 0) {
            return reserve.getNumberOfGuests();
        }
        return reserve.getDetails().size();
    }

    private UserEntity resolveMainClient(ReservationEntity reserve) {
        Long clientId = null;

        if (reserve.getCliente() != null && reserve.getCliente().getId() != null) {
            clientId = reserve.getCliente().getId();
        } else if (!reserve.getDetails().isEmpty()) {
            clientId = reserve.getDetails().get(0).getUserId();
        }

        if (clientId == null) {
            throw new BusinessException("No se pudo determinar el cliente principal de la reserva.");
        }

        final Long resolvedClientId = clientId;

        return userRepository.findById(resolvedClientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente principal no encontrado con ID: " + resolvedClientId));
    }

    private List<ReservationDetailsEntity> sortReservationDetails(List<ReservationDetailsEntity> details) {
        return details.stream()
                .peek(detail -> detail.setGuestName(detail.getGuestName().trim()))
                .sorted(Comparator.comparing(ReservationDetailsEntity::getGuestName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    private void recalculateReservationAmounts(ReservationEntity reserve) {
        int guestCount = reserve.getNumberOfGuests();
        long numberOfDays = calcularNumeroDias(reserve);
        double baseRate = calcularTarifaBase(reserve.getRoomType(), reserve.getStayType()) * numberOfDays;
        int maxBirthdayDiscounts = calcularMaxCumpleanos(guestCount);
        int appliedBirthdayDiscounts = 0;
        double totalAmount = 0.0;

        for (ReservationDetailsEntity detail : reserve.getDetails()) {
            UserEntity user = findUser(detail.getUserId());
            detail.setReservation(reserve);

            double birthdayDiscount = 0.0;
            if (appliedBirthdayDiscounts < maxBirthdayDiscounts
                    && esCumpleanos(user.getDateBirthday(), reserve.getCheckInDate())) {
                birthdayDiscount = 0.50;
                appliedBirthdayDiscounts++;
            }

            double loyaltyDiscount = userService.obtenerDescuentoPorCategoria(detail.getUserId());
            double groupDiscount = calcularDescuentoGrupo(guestCount);
            double finalDiscount = Math.max(birthdayDiscount, Math.max(loyaltyDiscount, groupDiscount));
            double finalAmount = baseRate * (1 - finalDiscount);

            detail.setDiscount(finalDiscount);
            detail.setFinalAmount(finalAmount);
            totalAmount += finalAmount;
        }

        reserve.setFinalAmount(totalAmount);
    }

    private UserEntity findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
    }

    private void updateVisitCounters(ReservationEntity reservation) {
        List<Long> userIds = reservation.getDetails().stream()
                .map(ReservationDetailsEntity::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        for (Long userId : userIds) {
            userService.incrementVisitsAndUpdateCategory(userId);
        }
    }

    private void copyReservationData(ReservationEntity target, ReservationEntity source) {
        target.setCheckInDate(source.getCheckInDate());
        target.setCheckOutDate(source.getCheckOutDate());
        target.setStayType(source.getStayType());
        target.setRoomType(source.getRoomType());
        target.setRoomId(source.getRoomId());
        target.setRoomNumber(source.getRoomNumber());
        target.setNumberOfGuests(source.getNumberOfGuests());
        target.setCliente(source.getCliente());
        target.setTouristPackageId(source.getTouristPackageId());
        target.setTouristPackageName(source.getTouristPackageName());
        target.getDetails().clear();
        if (source.getDetails() != null) {
            target.getDetails().addAll(source.getDetails());
        }
    }

    private TouristPackageEntity applyTouristPackageDataIfNeeded(ReservationEntity reserve) {
        if (reserve.getTouristPackageId() == null) {
            return null;
        }

        TouristPackageEntity touristPackage = touristPackageService.getPackageById(reserve.getTouristPackageId());

        if (touristPackage.getAvailableFrom() == null || touristPackage.getAvailableUntil() == null) {
            throw new BusinessException("El paquete turistico seleccionado no tiene fechas configuradas.");
        }

        reserve.setTouristPackageName(touristPackage.getPackageName());
        reserve.setCheckInDate(touristPackage.getAvailableFrom());
        reserve.setCheckOutDate(touristPackage.getAvailableUntil());
        reserve.setRoomType(touristPackage.getRoomType());
        reserve.setStayType("Completo");

        return touristPackage;
    }

    private void consumeTouristPackageSlotIfNeeded(TouristPackageEntity touristPackage) {
        if (touristPackage != null) {
            touristPackageService.reservePackageSlot(touristPackage.getId());
        }
    }

    private void assignSelectedRoom(ReservationEntity reserve) {
        if (reserve.getCheckInDate() == null || reserve.getCheckOutDate() == null) {
            throw new BusinessException("La reserva debe tener fechas para asignar una habitacion.");
        }

        if (reserve.getRoomType() == null || reserve.getRoomType().isBlank()) {
            throw new BusinessException("La reserva debe indicar el tipo de habitacion.");
        }

        var room = roomService.getAvailableRoomById(
                reserve.getRoomId(),
                reserve.getCheckInDate(),
                reserve.getCheckOutDate(),
                reserve.getRoomType(),
                reserve.getStayType()
        );

        reserve.setRoomNumber(room.getRoomNumber());
        reserve.setRoomType(room.getType());
    }

    private void validateActiveReservationLimit(String rut, Long currentReservationId) {
        if (rut == null || rut.isBlank()) {
            throw new BusinessException("No se pudo validar el limite de reservas activas del cliente.");
        }

        long activeReservations = reservationRepository.findByClienteRutIgnoreCaseAndCancelledFalse(rut).stream()
                .filter(reservation -> currentReservationId == null || !reservation.getId().equals(currentReservationId))
                .filter(this::isReservationActive)
                .count();

        if (activeReservations >= MAX_ACTIVE_RESERVATIONS_PER_RUT) {
            throw new BusinessException("El cliente con RUT " + rut + " ya tiene 3 reservas activas.");
        }
    }

    private boolean isReservationActive(ReservationEntity reservation) {
        if (reservation.getCheckOutDate() == null || reservation.getStayType() == null) {
            return false;
        }

        LocalDateTime reservationEnd = resolveReservationEnd(reservation.getCheckOutDate(), reservation.getStayType());
        return !reservationEnd.isBefore(LocalDateTime.now());
    }

    private LocalDateTime resolveReservationEnd(LocalDate date, String stayType) {
        String normalizedStayType = normalizeStayType(stayType);

        return switch (normalizedStayType) {
            case "manana" -> LocalDateTime.of(date, LocalTime.of(18, 30));
            case "noche" -> LocalDateTime.of(date, LocalTime.of(8, 30));
            case "completo" -> LocalDateTime.of(date, LocalTime.of(8, 30));
            default -> LocalDateTime.of(date, LocalTime.of(12, 0));
        };
    }

    private String normalizeStayType(String stayType) {
        if (stayType == null) {
            return "";
        }

        return Normalizer.normalize(stayType, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private List<ReservationEntity> filterReservationsBetween(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new BusinessException("Debe indicar fechaInicio y fechaFin para generar el reporte.");
        }

        if (fechaFin.isBefore(fechaInicio)) {
            throw new BusinessException("La fecha fin no puede ser anterior a la fecha inicio.");
        }

        return reservationRepository.findByCancelledFalse().stream()
                .peek(this::initializeReservation)
                .filter(reserva -> reserva.getCheckInDate() != null)
                .filter(reserva -> !reserva.getCheckInDate().isBefore(fechaInicio)
                        && !reserva.getCheckInDate().isAfter(fechaFin))
                .collect(Collectors.toList());
    }

    private void initializeReservation(ReservationEntity reservation) {
        if (reservation == null) {
            return;
        }

        if (reservation.getCliente() != null) {
            reservation.getCliente().getId();
        }

        if (reservation.getDetails() != null) {
            reservation.getDetails().forEach(detail -> {
                detail.getId();
                detail.getGuestName();
            });
        }
    }

    private List<String> getMeses() {
        return Arrays.asList(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
    }

    private Map<String, Double> createMonthlyAccumulator(List<String> meses) {
        Map<String, Double> ingresosPorMes = new LinkedHashMap<>();
        for (String mes : meses) {
            ingresosPorMes.put(mes, 0.0);
        }
        ingresosPorMes.put("TOTAL", 0.0);
        return ingresosPorMes;
    }

    private Map<String, Double> calculateTotals(Map<String, Map<String, Double>> reporte, List<String> meses) {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (String mes : meses) {
            double totalMes = reporte.values().stream()
                    .mapToDouble(ingresosPorMes -> ingresosPorMes.get(mes))
                    .sum();
            totals.put(mes, totalMes);
        }
        totals.put("TOTAL", totals.values().stream().mapToDouble(Double::doubleValue).sum());
        return totals;
    }
}
