package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.ReservationDetailsEntity;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Entities.TouristPackageEntity;
import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.DTOs.PackageRankingRow;
import kartingRM.Backend.DTOs.SalesReportRow;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final int MAX_ACTIVE_RESERVATIONS_PER_RUT = 3;
    private static final double GROUP_DISCOUNT = 0.10;
    private static final double MULTI_PACKAGE_DISCOUNT = 0.05;
    private static final double MAX_TOTAL_DISCOUNT = 0.20;

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
    // CRITICO: valida reglas principales de negocio, asigna habitacion, calcula descuentos y persiste la reserva.
    public ReservationEntity saveReservation(ReservationEntity reserve) {
        validateReservation(reserve);
        TouristPackageEntity selectedPackage = applyTouristPackageDataIfNeeded(reserve);
        UserEntity mainClient = resolveMainClient(reserve);
        validateReservationPeople(reserve, mainClient);
        validateActiveReservationLimit(mainClient.getRut(), null);
        reserve.setCliente(mainClient);
        prepareReservationForPersistence(reserve, selectedPackage);
        ReservationEntity savedReserve = reservationRepository.save(reserve);
        consumeTouristPackageSlotIfNeeded(selectedPackage, savedReserve.getNumberOfGuests());
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
        TouristPackageEntity selectedPackage = applyTouristPackageDataIfNeeded(existingReserve);
        UserEntity mainClient = resolveMainClient(existingReserve);
        validateReservationPeople(existingReserve, mainClient);
        validateActiveReservationLimit(mainClient.getRut(), id);
        existingReserve.setCliente(mainClient);
        prepareReservationForPersistence(existingReserve, selectedPackage);
        return reservationRepository.save(existingReserve);
    }

    @Transactional
    public void deleteReservation(Long id) {
        ReservationEntity reservation = getReservationById(id);
        if (reservation.getTouristPackageId() != null) {
            int slotsToRelease = reservation.getNumberOfGuests() == null ? 1 : reservation.getNumberOfGuests();
            for (int index = 0; index < slotsToRelease; index++) {
                touristPackageService.releasePackageSlot(reservation.getTouristPackageId());
            }
        }
        reservation.setCancelled(true);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);
    }

    @Transactional
    public ReservationEntity confirmReservationPayment(Long reservationId, Double paidAmount) {
        ReservationEntity reservation = getReservationById(reservationId);

        if (Boolean.TRUE.equals(reservation.getCancelled()) || "CANCELLED".equalsIgnoreCase(reservation.getStatus())) {
            throw new BusinessException("No se puede pagar una reserva cancelada.");
        }

        if (!"PENDING_PAYMENT".equalsIgnoreCase(reservation.getStatus())) {
            throw new BusinessException("La reserva no se encuentra pendiente de pago.");
        }

        double expectedAmount = reservation.getFinalAmount() == null ? 0.0 : reservation.getFinalAmount();
        if (paidAmount == null || Math.abs(paidAmount - expectedAmount) > 0.01) {
            throw new BusinessException("El pago debe corresponder al monto total de la reserva.");
        }

        reservation.setStatus("CONFIRMED");
        reservation.setPaidAt(LocalDateTime.now());
        reservation.setAmountPaid(paidAmount);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public ReservationEntity expireReservation(Long reservationId) {
        ReservationEntity reservation = getReservationById(reservationId);
        if (!"PENDING_PAYMENT".equalsIgnoreCase(reservation.getStatus())) {
            return reservation;
        }

        reservation.setStatus("EXPIRED");
        reservation.setCancelled(true);
        reservation.setCancelledAt(LocalDateTime.now());
        if (reservation.getTouristPackageId() != null) {
            int slotsToRelease = reservation.getNumberOfGuests() == null ? 1 : reservation.getNumberOfGuests();
            for (int index = 0; index < slotsToRelease; index++) {
                touristPackageService.releasePackageSlot(reservation.getTouristPackageId());
            }
        }
        return reservationRepository.save(reservation);
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

    @Transactional(readOnly = true)
    public List<SalesReportRow> getSalesReport(LocalDate fechaInicio, LocalDate fechaFin) {
        return filterReservationsBetween(fechaInicio, fechaFin).stream()
                .filter(reservation -> !"CANCELLED".equalsIgnoreCase(reservation.getStatus()))
                .sorted(Comparator.comparing(this::resolveOperationDate))
                .map(reservation -> new SalesReportRow(
                        resolveOperationDate(reservation),
                        reservation.getCliente() != null ? reservation.getCliente().getName() : "Cliente sin nombre",
                        reservation.getTouristPackageName() != null ? reservation.getTouristPackageName() : "Reserva sin paquete",
                        reservation.getNumberOfGuests(),
                        reservation.getFinalAmount(),
                        reservation.getAmountPaid() == null ? 0.0 : reservation.getAmountPaid(),
                        reservation.getStatus(),
                        reservation.getCheckInDate()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PackageRankingRow> getPackageRankingReport(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<Long, List<ReservationEntity>> reservationsByPackage = filterReservationsBetween(fechaInicio, fechaFin).stream()
                .filter(reservation -> reservation.getTouristPackageId() != null)
                .filter(reservation -> !"CANCELLED".equalsIgnoreCase(reservation.getStatus()))
                .collect(Collectors.groupingBy(
                        ReservationEntity::getTouristPackageId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return reservationsByPackage.entrySet().stream()
                .map(entry -> buildRankingRow(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(PackageRankingRow::passengerCount).reversed()
                        .thenComparing(PackageRankingRow::reservationsCount, Comparator.reverseOrder())
                        .thenComparing(PackageRankingRow::packageName, String.CASE_INSENSITIVE_ORDER))
                .toList();
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
        if (reserve == null) {
            throw new BusinessException("Debe enviar una reserva valida.");
        }

        boolean reservationFromPackage = reserve.getTouristPackageId() != null;

        if (reserve.getDetails() == null || reserve.getDetails().isEmpty()) {
            throw new BusinessException("La reserva debe incluir al menos un detalle.");
        }

        if (!reservationFromPackage && (reserve.getCheckInDate() == null || reserve.getCheckOutDate() == null)) {
            throw new BusinessException("La reserva debe incluir fecha de check-in y check-out.");
        }

        if (!reservationFromPackage && reserve.getCheckOutDate().isBefore(reserve.getCheckInDate())) {
            throw new BusinessException("La fecha de check-out no puede ser anterior al check-in.");
        }

        if (!reservationFromPackage && reserve.getCheckInDate().isBefore(LocalDate.now())) {
            throw new BusinessException("No se pueden registrar reservas con fecha de check-in en el pasado.");
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

    private void validateReservationPeople(ReservationEntity reserve, UserEntity mainClient) {
        if (reserve.getCheckInDate() == null) {
            throw new BusinessException("La reserva debe incluir una fecha de check-in valida.");
        }

        if (mainClient == null || mainClient.getId() == null) {
            throw new BusinessException("No se pudo determinar el huesped principal de la reserva.");
        }

        Set<Long> uniqueGuestIds = new HashSet<>();
        boolean mainClientIncluded = false;

        for (ReservationDetailsEntity detail : reserve.getDetails()) {
            if (!uniqueGuestIds.add(detail.getUserId())) {
                throw new BusinessException("No se puede repetir el mismo huesped dentro de una reserva.");
            }

            UserEntity guest = findUser(detail.getUserId());
            if (mainClient.getId().equals(guest.getId())) {
                mainClientIncluded = true;
            }

            userService.validateReservationGuestAge(guest, reserve.getCheckInDate(), false);
        }

        if (!mainClientIncluded) {
            throw new BusinessException("El huesped principal debe estar incluido entre los detalles de la reserva.");
        }

        userService.validateReservationGuestAge(mainClient, reserve.getCheckInDate(), true);
    }

    private void prepareReservationForPersistence(ReservationEntity reserve, TouristPackageEntity selectedPackage) {
        if (reserve.getCancelled() == null) {
            reserve.setCancelled(Boolean.FALSE);
        }

        if (reserve.getStatus() == null || reserve.getStatus().isBlank()) {
            reserve.setStatus("PENDING_PAYMENT");
        }

        if (reserve.getCreatedAt() == null) {
            reserve.setCreatedAt(LocalDateTime.now());
        }

        if (reserve.getExpiresAt() == null && "PENDING_PAYMENT".equalsIgnoreCase(reserve.getStatus())) {
            reserve.setExpiresAt(reserve.getCreatedAt().plusHours(24));
        }

        reserve.setNumberOfGuests(resolveGuestCount(reserve));
        reserve.setStayType(reserve.getStayType().trim());
        reserve.setRoomType(reserve.getRoomType().trim());
        assignSelectedRoom(reserve);
        reserve.setDetails(sortReservationDetails(reserve.getDetails()));
        recalculateReservationAmounts(reserve, selectedPackage);
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

    private void recalculateReservationAmounts(ReservationEntity reserve, TouristPackageEntity selectedPackage) {
        int guestCount = reserve.getNumberOfGuests();
        double baseRate = resolveBaseRate(reserve);
        DiscountCalculation discountCalculation = calculateDiscounts(reserve, selectedPackage);
        int maxBirthdayDiscounts = calcularMaxCumpleanos(guestCount);
        int appliedBirthdayDiscounts = 0;
        double totalAmount = 0.0;
        double originalAmount = 0.0;

        for (ReservationDetailsEntity detail : reserve.getDetails()) {
            UserEntity user = findUser(detail.getUserId());
            detail.setReservation(reserve);

            double birthdayDiscount = 0.0;
            if (appliedBirthdayDiscounts < maxBirthdayDiscounts
                    && esCumpleanos(user.getDateBirthday(), reserve.getCheckInDate())) {
                birthdayDiscount = 0.50;
                appliedBirthdayDiscounts++;
            }

            double loyaltyDiscount = reserve.getTouristPackageId() == null
                    ? userService.obtenerDescuentoPorCategoria(detail.getUserId())
                    : 0.0;
            double finalDiscount = Math.max(birthdayDiscount, Math.max(loyaltyDiscount, discountCalculation.totalDiscount()));
            double finalAmount = baseRate * (1 - finalDiscount);

            detail.setDiscount(finalDiscount);
            detail.setFinalAmount(finalAmount);
            originalAmount += baseRate;
            totalAmount += finalAmount;
        }

        reserve.setOriginalAmount(originalAmount);
        reserve.setFinalAmount(totalAmount);
        reserve.setDiscountAmount(Math.max(0.0, originalAmount - totalAmount));
        reserve.setDiscountPercent(originalAmount <= 0 ? 0.0 : reserve.getDiscountAmount() / originalAmount);
        reserve.setDiscountBreakdown(discountCalculation.breakdown());
    }

    private double resolveBaseRate(ReservationEntity reserve) {
        if (reserve.getTouristPackageId() != null && reserve.getTouristPackagePrice() != null) {
            return reserve.getTouristPackagePrice();
        }

        long numberOfDays = calcularNumeroDias(reserve);
        return calcularTarifaBase(reserve.getRoomType(), reserve.getStayType()) * numberOfDays;
    }

    private DiscountCalculation calculateDiscounts(ReservationEntity reserve, TouristPackageEntity selectedPackage) {
        if (reserve.getTouristPackageId() == null) {
            return new DiscountCalculation(calcularDescuentoGrupo(reserve.getNumberOfGuests()), "descuento por grupo");
        }

        Set<String> reasons = new LinkedHashSet<>();
        double discount = 0.0;

        if (reserve.getNumberOfGuests() != null && reserve.getNumberOfGuests() >= 4) {
            discount += GROUP_DISCOUNT;
            reasons.add("descuento por grupo");
        }

        if (reserve.getCliente() != null && reserve.getCliente().getId() != null
                && countPaidHistoricalReservations(reserve.getCliente().getId()) >= 3) {
            discount += Math.max(0.10, userService.obtenerDescuentoPorCategoria(reserve.getCliente().getId()));
            reasons.add("cliente frecuente");
        }

        if (hasRecentPackagePurchase(reserve)) {
            discount += MULTI_PACKAGE_DISCOUNT;
            reasons.add("compra de multiples paquetes");
        }

        if (hasActivePromotion(selectedPackage, LocalDate.now())) {
            discount += selectedPackage.getPromotionDiscountPercent() / 100.0;
            reasons.add("promocion por tiempo limitado");
        }

        double cappedDiscount = Math.min(discount, MAX_TOTAL_DISCOUNT);
        if (discount > cappedDiscount) {
            reasons.add("limite maximo de descuentos");
        }

        return new DiscountCalculation(cappedDiscount, reasons.isEmpty() ? "sin descuentos aplicados" : String.join(", ", reasons));
    }

    private long countPaidHistoricalReservations(Long clientId) {
        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getCliente() != null && clientId.equals(reservation.getCliente().getId()))
                .filter(reservation -> "CONFIRMED".equalsIgnoreCase(reservation.getStatus()))
                .count();
    }

    private boolean hasRecentPackagePurchase(ReservationEntity reserve) {
        if (reserve.getCliente() == null || reserve.getCliente().getId() == null || reserve.getCreatedAt() == null) {
            return false;
        }

        LocalDateTime periodStart = reserve.getCreatedAt().minusDays(30);
        return reservationRepository.findAll().stream()
                .filter(existing -> existing.getTouristPackageId() != null)
                .filter(existing -> existing.getCliente() != null && reserve.getCliente().getId().equals(existing.getCliente().getId()))
                .filter(existing -> existing.getCreatedAt() != null && !existing.getCreatedAt().isBefore(periodStart))
                .filter(existing -> !"CANCELLED".equalsIgnoreCase(existing.getStatus()))
                .anyMatch(existing -> existing.getId() == null || reserve.getId() == null || !existing.getId().equals(reserve.getId()));
    }

    private boolean hasActivePromotion(TouristPackageEntity touristPackage, LocalDate date) {
        return touristPackage != null
                && Boolean.TRUE.equals(touristPackage.getPromotionActive())
                && touristPackage.getPromotionDiscountPercent() != null
                && touristPackage.getPromotionDiscountPercent() > 0
                && touristPackage.getPromotionStartDate() != null
                && touristPackage.getPromotionEndDate() != null
                && !date.isBefore(touristPackage.getPromotionStartDate())
                && !date.isAfter(touristPackage.getPromotionEndDate());
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
        target.setTouristPackagePrice(source.getTouristPackagePrice());
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

        int guestCount = resolveGuestCount(reserve);
        if (!touristPackageService.isPubliclyBookable(touristPackage, LocalDate.now())) {
            throw new BusinessException("No se puede registrar una reserva para un paquete no vigente, agotado o cancelado.");
        }

        if (touristPackage.getAvailableSlots() < guestCount) {
            throw new BusinessException("La cantidad solicitada excede los cupos disponibles del paquete.");
        }

        if (touristPackage.getMaxGuests() != null && guestCount > touristPackage.getMaxGuests()) {
            throw new BusinessException("La cantidad de pasajeros excede la capacidad maxima del paquete.");
        }

        reserve.setTouristPackageName(touristPackage.getPackageName());
        reserve.setTouristPackagePrice(touristPackage.getPrice());
        reserve.setCheckInDate(touristPackage.getAvailableFrom());
        reserve.setCheckOutDate(touristPackage.getAvailableUntil());
        reserve.setRoomType(touristPackage.getRoomType());
        reserve.setStayType("Completo");

        return touristPackage;
    }

    private void consumeTouristPackageSlotIfNeeded(TouristPackageEntity touristPackage, int guestCount) {
        if (touristPackage != null) {
            touristPackageService.reservePackageSlots(touristPackage.getId(), guestCount);
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
                .filter(reserva -> isReservationInsidePeriod(reserva, fechaInicio, fechaFin))
                .collect(Collectors.toList());
    }

    private boolean isReservationInsidePeriod(ReservationEntity reservation, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDate operationDate = resolveOperationDate(reservation).toLocalDate();
        return !operationDate.isBefore(fechaInicio) && !operationDate.isAfter(fechaFin);
    }

    private LocalDateTime resolveOperationDate(ReservationEntity reservation) {
        if (reservation.getPaidAt() != null) {
            return reservation.getPaidAt();
        }
        if (reservation.getCreatedAt() != null) {
            return reservation.getCreatedAt();
        }
        if (reservation.getCheckInDate() != null) {
            return reservation.getCheckInDate().atStartOfDay();
        }
        return LocalDateTime.MIN;
    }

    private PackageRankingRow buildRankingRow(Long packageId, List<ReservationEntity> reservations) {
        ReservationEntity sample = reservations.get(0);
        long passengerCount = reservations.stream()
                .mapToLong(reservation -> reservation.getNumberOfGuests() == null ? 0 : reservation.getNumberOfGuests())
                .sum();
        double totalAmount = reservations.stream()
                .mapToDouble(reservation -> reservation.getFinalAmount() == null ? 0.0 : reservation.getFinalAmount())
                .sum();

        return new PackageRankingRow(
                packageId,
                sample.getTouristPackageName() == null ? "Paquete " + packageId : sample.getTouristPackageName(),
                (long) reservations.size(),
                passengerCount,
                totalAmount
        );
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

    private record DiscountCalculation(double totalDiscount, String breakdown) {
    }
}
