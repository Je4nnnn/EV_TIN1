package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.TouristPackageEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.TouristPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TouristPackageService {

    @Autowired
    private TouristPackageRepository touristPackageRepository;

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
        return packages;
    }

    @Transactional(readOnly = true)
    public TouristPackageEntity getPackageById(Long id) {
        TouristPackageEntity touristPackage = touristPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete turistico no encontrado con ID: " + id));
        normalizePackageForRead(touristPackage);
        return touristPackage;
    }

    @Transactional
    public TouristPackageEntity reservePackageSlot(Long id) {
        TouristPackageEntity touristPackage = getPackageById(id);

        if (!Boolean.TRUE.equals(touristPackage.getAvailable())) {
            throw new BusinessException("El paquete turistico seleccionado no esta disponible.");
        }

        if (touristPackage.getAvailableSlots() == null || touristPackage.getAvailableSlots() <= 0) {
            throw new BusinessException("El paquete turistico no tiene cupos disponibles.");
        }

        if (touristPackage.getAvailableFrom() == null || touristPackage.getAvailableUntil() == null) {
            throw new BusinessException("El paquete turistico debe tener fechas disponibles para poder reservarse.");
        }

        touristPackage.setAvailableSlots(touristPackage.getAvailableSlots() - 1);
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
        existingPackage.setTransferIncluded(payload.getTransferIncluded());
        existingPackage.setAutomobileServiceIncluded(payload.getAutomobileServiceIncluded());
        existingPackage.setPrice(payload.getPrice());
        existingPackage.setAvailableSlots(payload.getAvailableSlots());
        existingPackage.setStatus(payload.getStatus());
        existingPackage.setAvailable(payload.getAvailable());
        existingPackage.setMaxGuests(payload.getMaxGuests());
        existingPackage.setAvailableFrom(payload.getAvailableFrom());
        existingPackage.setAvailableUntil(payload.getAvailableUntil());

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

    public void deletePackage(Long id) {
        TouristPackageEntity touristPackage = getPackageById(id);
        touristPackageRepository.delete(touristPackage);
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
    }

    private void normalizePackage(TouristPackageEntity touristPackage) {
        touristPackage.setPackageName(touristPackage.getPackageName().trim());
        touristPackage.setDescription(touristPackage.getDescription().trim());
        touristPackage.setDestinations(cleanList(touristPackage.getDestinations()));
        touristPackage.setActivities(cleanList(touristPackage.getActivities()));
        touristPackage.setExtraServices(cleanList(touristPackage.getExtraServices()));
        touristPackage.setRoomType(touristPackage.getRoomType().trim());
        touristPackage.setStatus(touristPackage.getStatus().trim().toUpperCase());
        touristPackage.setAvailable(Boolean.TRUE.equals(touristPackage.getAvailable()) && touristPackage.getAvailableSlots() > 0);
        touristPackage.setTransferIncluded(Boolean.TRUE.equals(touristPackage.getTransferIncluded()));
        touristPackage.setAutomobileServiceIncluded(Boolean.TRUE.equals(touristPackage.getAutomobileServiceIncluded()));
    }

    private void validateAvailabilityWindow(LocalDate availableFrom, LocalDate availableUntil) {
        if (availableFrom != null && availableUntil != null && availableUntil.isBefore(availableFrom)) {
            throw new BusinessException("La fecha fin de disponibilidad no puede ser anterior a la fecha inicio.");
        }
    }

    private List<String> cleanList(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void normalizePackageForRead(TouristPackageEntity touristPackage) {
        if (touristPackage == null) {
            return;
        }

        if (touristPackage.getDestinations() == null) {
            touristPackage.setDestinations(new java.util.ArrayList<>());
        }
        if (touristPackage.getActivities() == null) {
            touristPackage.setActivities(new java.util.ArrayList<>());
        }
        if (touristPackage.getExtraServices() == null) {
            touristPackage.setExtraServices(new java.util.ArrayList<>());
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

        touristPackage.getDestinations().size();
        touristPackage.getActivities().size();
        touristPackage.getExtraServices().size();
    }
}
