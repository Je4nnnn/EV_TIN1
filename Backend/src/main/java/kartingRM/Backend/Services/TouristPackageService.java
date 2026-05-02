package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.TouristPackageEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.ReservationRepository;
import kartingRM.Backend.Repositories.TouristPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TouristPackageService {

    @Autowired
    private TouristPackageRepository touristPackageRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<TouristPackageEntity> getAllPackages() {
        List<TouristPackageEntity> packages = touristPackageRepository.findAll();
        packages.forEach(this::normalizePackageForRead);
        return packages;
    }

    @Transactional(readOnly = true)
    public List<TouristPackageEntity> getAvailablePackages() {
        List<TouristPackageEntity> packages = touristPackageRepository.findByAvailableTrueOrderByPackageNameAsc();
        packages.forEach(this::normalizePackageForRead);
        LocalDate today = LocalDate.now();
        return packages.stream()
                .filter(touristPackage -> isPubliclyBookable(touristPackage, today))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TouristPackageEntity> searchAvailablePackages(
            String destination,
            LocalDate startDate,
            LocalDate endDate,
            Double minPrice,
            Double maxPrice,
            Integer minDays,
            Integer maxDays,
            String travelType
    ) {
        return getAvailablePackages().stream()
                .filter(touristPackage -> matchesDestination(touristPackage, destination))
                .filter(touristPackage -> startDate == null || !touristPackage.getAvailableFrom().isBefore(startDate))
                .filter(touristPackage -> endDate == null || !touristPackage.getAvailableUntil().isAfter(endDate))
                .filter(touristPackage -> minPrice == null || touristPackage.getPrice() >= minPrice)
                .filter(touristPackage -> maxPrice == null || touristPackage.getPrice() <= maxPrice)
                .filter(touristPackage -> minDays == null || touristPackage.getDaysCount() >= minDays)
                .filter(touristPackage -> maxDays == null || touristPackage.getDaysCount() <= maxDays)
                .filter(touristPackage -> isBlank(travelType)
                        || (touristPackage.getTravelType() != null
                        && touristPackage.getTravelType().equalsIgnoreCase(travelType.trim())))
                .toList();
    }

    @Transactional(readOnly = true)
    public TouristPackageEntity getPackageById(Long id) {
        TouristPackageEntity touristPackage = touristPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete turistico no encontrado con ID: " + id));
        normalizePackageForRead(touristPackage);
        return touristPackage;
    }

    @Transactional
    // CRITICO: controla cupos, estado y validez de fechas al consumir disponibilidad de un paquete.
    public TouristPackageEntity reservePackageSlot(Long id) {
        return reservePackageSlots(id, 1);
    }

    @Transactional
    public TouristPackageEntity reservePackageSlots(Long id, int requestedSlots) {
        TouristPackageEntity touristPackage = getPackageById(id);

        if (requestedSlots <= 0) {
            throw new BusinessException("La cantidad de cupos solicitada debe ser mayor a cero.");
        }

        if (!Boolean.TRUE.equals(touristPackage.getAvailable())) {
            throw new BusinessException("El paquete turistico seleccionado no esta disponible.");
        }

        if (touristPackage.getAvailableSlots() == null || touristPackage.getAvailableSlots() <= 0) {
            throw new BusinessException("El paquete turistico no tiene cupos disponibles.");
        }

        if (touristPackage.getAvailableFrom() == null || touristPackage.getAvailableUntil() == null) {
            throw new BusinessException("El paquete turistico debe tener fechas disponibles para poder reservarse.");
        }

        if (!isPubliclyBookable(touristPackage, LocalDate.now())) {
            throw new BusinessException("El paquete turistico no se encuentra vigente para clientes.");
        }

        if (touristPackage.getAvailableSlots() < requestedSlots) {
            throw new BusinessException("La cantidad solicitada excede los cupos disponibles del paquete.");
        }

        touristPackage.setAvailableSlots(touristPackage.getAvailableSlots() - requestedSlots);
        touristPackage.setAvailable(touristPackage.getAvailableSlots() > 0);
        if (!touristPackage.getAvailable()) {
            touristPackage.setStatus("UNAVAILABLE");
        }

        return touristPackageRepository.save(touristPackage);
    }

    @Transactional
    public TouristPackageEntity releasePackageSlot(Long id) {
        TouristPackageEntity touristPackage = getPackageById(id);

        int currentSlots = touristPackage.getAvailableSlots() == null ? 0 : touristPackage.getAvailableSlots();
        touristPackage.setAvailableSlots(currentSlots + 1);
        touristPackage.setAvailable(true);
        touristPackage.setStatus("AVAILABLE");

        return touristPackageRepository.save(touristPackage);
    }

    @Transactional
    public TouristPackageEntity createPackage(TouristPackageEntity touristPackage) {
        validatePackage(touristPackage);
        normalizePackage(touristPackage);
        return touristPackageRepository.save(touristPackage);
    }

    @Transactional
    public TouristPackageEntity updatePackage(Long id, TouristPackageEntity payload) {
        TouristPackageEntity existingPackage = getPackageById(id);

        existingPackage.setPackageName(payload.getPackageName());
        existingPackage.setDescription(payload.getDescription());
        existingPackage.setDestinations(payload.getDestinations());
        existingPackage.setActivities(payload.getActivities());
        existingPackage.setExtraServices(payload.getExtraServices());
        existingPackage.setDaysCount(payload.getDaysCount());
        existingPackage.setNightsCount(payload.getNightsCount());
        existingPackage.setRoomType(payload.getRoomType());
        existingPackage.setTravelType(payload.getTravelType());
        existingPackage.setSeason(payload.getSeason());
        existingPackage.setCategory(payload.getCategory());
        existingPackage.setTransferIncluded(payload.getTransferIncluded());
        existingPackage.setAutomobileServiceIncluded(payload.getAutomobileServiceIncluded());
        existingPackage.setPrice(payload.getPrice());
        existingPackage.setAvailableSlots(payload.getAvailableSlots());
        existingPackage.setStatus(payload.getStatus());
        existingPackage.setAvailable(payload.getAvailable());
        existingPackage.setMaxGuests(payload.getMaxGuests());
        existingPackage.setAvailableFrom(payload.getAvailableFrom());
        existingPackage.setAvailableUntil(payload.getAvailableUntil());
        existingPackage.setPromotionActive(payload.getPromotionActive());
        existingPackage.setPromotionDiscountPercent(payload.getPromotionDiscountPercent());
        existingPackage.setPromotionStartDate(payload.getPromotionStartDate());
        existingPackage.setPromotionEndDate(payload.getPromotionEndDate());

        validatePackage(existingPackage);
        normalizePackage(existingPackage);
        return touristPackageRepository.save(existingPackage);
    }

    @Transactional
    public TouristPackageEntity updateAvailability(Long id, boolean available) {
        TouristPackageEntity touristPackage = getPackageById(id);
        touristPackage.setAvailable(available);
        touristPackage.setStatus(available ? "AVAILABLE" : "UNAVAILABLE");
        return touristPackageRepository.save(touristPackage);
    }

    private void validatePackage(TouristPackageEntity touristPackage) {
        if (touristPackage == null) {
            throw new BusinessException("Debe enviar un paquete turistico valido.");
        }

        if (isBlank(touristPackage.getPackageName())) {
            throw new BusinessException("El nombre del paquete es obligatorio.");
        }

        if (isBlank(touristPackage.getDescription())) {
            throw new BusinessException("La descripcion del paquete es obligatoria.");
        }

        if (touristPackage.getDestinations() == null || touristPackage.getDestinations().isEmpty()) {
            throw new BusinessException("Debe indicar al menos un destino para el paquete.");
        }

        if (touristPackage.getDaysCount() == null || touristPackage.getDaysCount() <= 0) {
            throw new BusinessException("La cantidad de dias debe ser mayor a cero.");
        }

        if (touristPackage.getNightsCount() == null || touristPackage.getNightsCount() < 0) {
            throw new BusinessException("La cantidad de noches no puede ser negativa.");
        }

        if (touristPackage.getNightsCount() > touristPackage.getDaysCount()) {
            throw new BusinessException("La cantidad de noches no puede superar la cantidad de dias.");
        }

        if (isBlank(touristPackage.getRoomType())) {
            throw new BusinessException("El tipo de habitacion del paquete es obligatorio.");
        }

        if (touristPackage.getPrice() == null || touristPackage.getPrice() <= 0) {
            throw new BusinessException("El precio del paquete debe ser mayor a cero.");
        }

        if (touristPackage.getAvailableSlots() == null || touristPackage.getAvailableSlots() < 0) {
            throw new BusinessException("Los cupos disponibles no pueden ser negativos.");
        }

        if (touristPackage.getMaxGuests() == null || touristPackage.getMaxGuests() <= 0) {
            throw new BusinessException("La capacidad maxima del paquete debe ser mayor a cero.");
        }

        if (isBlank(touristPackage.getStatus())) {
            throw new BusinessException("El estado del paquete es obligatorio.");
        }

        validateAvailabilityWindow(touristPackage.getAvailableFrom(), touristPackage.getAvailableUntil());
        validatePromotionWindow(touristPackage);
    }

    private void normalizePackage(TouristPackageEntity touristPackage) {
        touristPackage.setPackageName(touristPackage.getPackageName().trim());
        touristPackage.setDescription(touristPackage.getDescription().trim());
        touristPackage.setDestinations(cleanList(touristPackage.getDestinations()));
        touristPackage.setActivities(cleanList(touristPackage.getActivities()));
        touristPackage.setExtraServices(cleanList(touristPackage.getExtraServices()));
        touristPackage.setRoomType(touristPackage.getRoomType().trim());
        touristPackage.setTravelType(cleanOptional(touristPackage.getTravelType(), "GENERAL"));
        touristPackage.setSeason(cleanOptional(touristPackage.getSeason(), "REGULAR"));
        touristPackage.setCategory(cleanOptional(touristPackage.getCategory(), "STANDARD"));
        touristPackage.setStatus(touristPackage.getStatus().trim().toUpperCase());
        touristPackage.setAvailable(Boolean.TRUE.equals(touristPackage.getAvailable())
                && touristPackage.getAvailableSlots() > 0
                && isPubliclyBookable(touristPackage, LocalDate.now()));
        touristPackage.setTransferIncluded(Boolean.TRUE.equals(touristPackage.getTransferIncluded()));
        touristPackage.setAutomobileServiceIncluded(Boolean.TRUE.equals(touristPackage.getAutomobileServiceIncluded()));
        touristPackage.setPromotionActive(Boolean.TRUE.equals(touristPackage.getPromotionActive()));
        if (touristPackage.getPromotionDiscountPercent() == null || !touristPackage.getPromotionActive()) {
            touristPackage.setPromotionDiscountPercent(0.0);
        }
    }

    private void validateAvailabilityWindow(LocalDate availableFrom, LocalDate availableUntil) {
        if (availableFrom != null && availableUntil != null && availableUntil.isBefore(availableFrom)) {
            throw new BusinessException("La fecha fin de disponibilidad no puede ser anterior a la fecha inicio.");
        }
    }

    private void validatePromotionWindow(TouristPackageEntity touristPackage) {
        if (!Boolean.TRUE.equals(touristPackage.getPromotionActive())) {
            return;
        }

        if (touristPackage.getPromotionDiscountPercent() == null
                || touristPackage.getPromotionDiscountPercent() <= 0
                || touristPackage.getPromotionDiscountPercent() > 100) {
            throw new BusinessException("El descuento de la promocion debe estar entre 0 y 100.");
        }

        if (touristPackage.getPromotionStartDate() == null || touristPackage.getPromotionEndDate() == null) {
            throw new BusinessException("La promocion debe tener fecha de inicio y termino.");
        }

        if (touristPackage.getPromotionEndDate().isBefore(touristPackage.getPromotionStartDate())) {
            throw new BusinessException("La fecha termino de promocion no puede ser anterior a la fecha inicio.");
        }
    }

    private List<String> cleanList(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }

        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String cleanOptional(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim().toUpperCase();
    }

    private boolean matchesDestination(TouristPackageEntity touristPackage, String destination) {
        if (isBlank(destination)) {
            return true;
        }

        String normalizedDestination = destination.trim().toLowerCase();
        return touristPackage.getDestinations().stream()
                .anyMatch(value -> value.toLowerCase().contains(normalizedDestination));
    }

    public boolean isPubliclyBookable(TouristPackageEntity touristPackage, LocalDate referenceDate) {
        if (touristPackage == null || referenceDate == null) {
            return false;
        }

        return Boolean.TRUE.equals(touristPackage.getAvailable())
                && touristPackage.getAvailableSlots() != null
                && touristPackage.getAvailableSlots() > 0
                && "AVAILABLE".equalsIgnoreCase(touristPackage.getStatus())
                && touristPackage.getAvailableFrom() != null
                && touristPackage.getAvailableUntil() != null
                && !touristPackage.getAvailableUntil().isBefore(referenceDate)
                && !touristPackage.getAvailableUntil().isBefore(touristPackage.getAvailableFrom());
    }

    private void normalizePackageForRead(TouristPackageEntity touristPackage) {
        if (touristPackage == null) {
            return;
        }

        if (touristPackage.getDestinations() == null) {
            touristPackage.setDestinations(new ArrayList<>());
        } else {
            touristPackage.setDestinations(new ArrayList<>(touristPackage.getDestinations()));
        }
        if (touristPackage.getActivities() == null) {
            touristPackage.setActivities(new ArrayList<>());
        } else {
            touristPackage.setActivities(new ArrayList<>(touristPackage.getActivities()));
        }
        if (touristPackage.getExtraServices() == null) {
            touristPackage.setExtraServices(new ArrayList<>());
        } else {
            touristPackage.setExtraServices(new ArrayList<>(touristPackage.getExtraServices()));
        }
        if (touristPackage.getAvailable() == null) {
            touristPackage.setAvailable(Boolean.FALSE);
        }
        if (touristPackage.getTransferIncluded() == null) {
            touristPackage.setTransferIncluded(Boolean.FALSE);
        }
        if (touristPackage.getAutomobileServiceIncluded() == null) {
            touristPackage.setAutomobileServiceIncluded(Boolean.FALSE);
        }
        if (touristPackage.getStatus() == null || touristPackage.getStatus().isBlank()) {
            touristPackage.setStatus(Boolean.TRUE.equals(touristPackage.getAvailable()) ? "AVAILABLE" : "UNAVAILABLE");
        }
        if (touristPackage.getTravelType() == null || touristPackage.getTravelType().isBlank()) {
            touristPackage.setTravelType("GENERAL");
        }
        if (touristPackage.getSeason() == null || touristPackage.getSeason().isBlank()) {
            touristPackage.setSeason("REGULAR");
        }
        if (touristPackage.getCategory() == null || touristPackage.getCategory().isBlank()) {
            touristPackage.setCategory("STANDARD");
        }
        if (touristPackage.getPromotionActive() == null) {
            touristPackage.setPromotionActive(Boolean.FALSE);
        }
        if (touristPackage.getPromotionDiscountPercent() == null) {
            touristPackage.setPromotionDiscountPercent(0.0);
        }

        touristPackage.getDestinations().size();
        touristPackage.getActivities().size();
        touristPackage.getExtraServices().size();
    }

    @Transactional
    public void deletePackage(Long id) {
        TouristPackageEntity touristPackage = getPackageById(id);
        if (reservationRepository.existsByTouristPackageIdAndCancelledFalse(id)) {
            touristPackage.setAvailable(false);
            touristPackage.setStatus("CANCELLED");
            touristPackageRepository.save(touristPackage);
            return;
        }

        touristPackageRepository.delete(touristPackage);
    }
}
