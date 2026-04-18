package kartingRM.backend.services;

import kartingRM.Backend.Entities.TouristPackageEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.TouristPackageRepository;
import kartingRM.Backend.Services.TouristPackageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TouristPackageServiceTest {

    @Mock
    private TouristPackageRepository touristPackageRepository;

    @InjectMocks
    private TouristPackageService touristPackageService;

    @Test
    void createPackage_givenValidPackage_whenCreated_thenNormalizesAndSaves() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setPackageName("  Patagonia  ");
        touristPackage.setStatus(" available ");
        when(touristPackageRepository.save(any(TouristPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        TouristPackageEntity created = touristPackageService.createPackage(touristPackage);

        // THEN
        assertEquals("Patagonia", created.getPackageName());
        assertEquals("AVAILABLE", created.getStatus());
        assertTrue(created.getAvailable());
    }

    @Test
    void createPackage_givenNullPackage_whenCreated_thenThrowsBusinessException() {
        // GIVEN - WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> touristPackageService.createPackage(null));

        // THEN
        assertEquals("Debe enviar un paquete turistico valido.", exception.getMessage());
    }

    @Test
    void createPackage_givenBlankName_whenCreated_thenThrowsBusinessException() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setPackageName(" ");

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> touristPackageService.createPackage(touristPackage));

        // THEN
        assertEquals("El nombre del paquete es obligatorio.", exception.getMessage());
    }

    @Test
    void createPackage_givenNegativeSlots_whenCreated_thenThrowsBusinessException() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setAvailableSlots(-1);

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> touristPackageService.createPackage(touristPackage));

        // THEN
        assertEquals("Los cupos disponibles no pueden ser negativos.", exception.getMessage());
    }

    @Test
    void createPackage_givenInvalidAvailabilityWindow_whenCreated_thenThrowsBusinessException() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setAvailableFrom(LocalDate.of(2026, 4, 20));
        touristPackage.setAvailableUntil(LocalDate.of(2026, 4, 19));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> touristPackageService.createPackage(touristPackage));

        // THEN
        assertEquals("La fecha fin de disponibilidad no puede ser anterior a la fecha inicio.", exception.getMessage());
    }

    @Test
    void reservePackageSlot_givenAvailablePackageWithRemainingSlots_whenReserved_thenDecrementsAndKeepsAvailable() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setAvailableSlots(3);
        touristPackage.setAvailable(true);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));
        when(touristPackageRepository.save(any(TouristPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        TouristPackageEntity reserved = touristPackageService.reservePackageSlot(1L);

        // THEN
        assertEquals(2, reserved.getAvailableSlots());
        assertTrue(reserved.getAvailable());
        assertEquals("AVAILABLE", reserved.getStatus());
    }

    @Test
    void reservePackageSlot_givenLastSlot_whenReserved_thenMarksPackageUnavailable() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setAvailableSlots(1);
        touristPackage.setAvailable(true);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));
        when(touristPackageRepository.save(any(TouristPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        TouristPackageEntity reserved = touristPackageService.reservePackageSlot(1L);

        // THEN
        assertEquals(0, reserved.getAvailableSlots());
        assertFalse(reserved.getAvailable());
        assertEquals("UNAVAILABLE", reserved.getStatus());
    }

    @Test
    void reservePackageSlot_givenUnavailableFlag_whenReserved_thenThrowsBusinessException() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setAvailable(false);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> touristPackageService.reservePackageSlot(1L));

        // THEN
        assertEquals("El paquete turistico seleccionado no esta disponible.", exception.getMessage());
    }

    @Test
    void reservePackageSlot_givenZeroSlots_whenReserved_thenThrowsBusinessException() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setAvailableSlots(0);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> touristPackageService.reservePackageSlot(1L));

        // THEN
        assertEquals("El paquete turistico no tiene cupos disponibles.", exception.getMessage());
    }

    @Test
    void reservePackageSlot_givenMissingAvailabilityDates_whenReserved_thenThrowsBusinessException() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setAvailableFrom(null);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> touristPackageService.reservePackageSlot(1L));

        // THEN
        assertEquals("El paquete turistico debe tener fechas disponibles para poder reservarse.", exception.getMessage());
    }

    @Test
    void reservePackageSlot_givenNegativeSlots_whenReserved_thenThrowsBusinessException() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setAvailableSlots(-3);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> touristPackageService.reservePackageSlot(1L));

        // THEN
        assertEquals("El paquete turistico no tiene cupos disponibles.", exception.getMessage());
    }

    @Test
    void reservePackageSlot_givenNullSlots_whenReserved_thenThrowsBusinessException() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setAvailableSlots(null);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> touristPackageService.reservePackageSlot(1L));

        // THEN
        assertEquals("El paquete turistico no tiene cupos disponibles.", exception.getMessage());
    }

    @Test
    void reservePackageSlot_givenPackageWithoutAvailabilityFlag_whenReserved_thenTreatsItAsUnavailable() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setAvailable(null);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> touristPackageService.reservePackageSlot(1L));

        // THEN
        assertEquals("El paquete turistico seleccionado no esta disponible.", exception.getMessage());
    }

    @Test
    void reservePackageSlot_givenUnknownPackage_whenReserved_thenThrowsResourceNotFoundException() {
        // GIVEN
        when(touristPackageRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> touristPackageService.reservePackageSlot(999L));

        // THEN
        assertEquals("Paquete turistico no encontrado con ID: 999", exception.getMessage());
    }

    @Test
    void reservePackageSlot_givenBlankStatusAndLastSlot_whenReserved_thenLeavesPackageUnavailable() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setStatus(" ");
        touristPackage.setAvailableSlots(1);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));
        when(touristPackageRepository.save(any(TouristPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        TouristPackageEntity reserved = touristPackageService.reservePackageSlot(1L);

        // THEN
        assertEquals(0, reserved.getAvailableSlots());
        assertFalse(reserved.getAvailable());
        assertEquals("UNAVAILABLE", reserved.getStatus());
    }

    @Test
    void reservePackageSlot_givenZeroSlotsButAvailableTrue_whenReserved_thenRejectsReservation() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setAvailable(true);
        touristPackage.setAvailableSlots(0);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> touristPackageService.reservePackageSlot(1L));

        // THEN
        assertEquals("El paquete turistico no tiene cupos disponibles.", exception.getMessage());
    }

    @Test
    void reservePackageSlot_givenNullStatusAndMultipleSlots_whenReserved_thenKeepsAvailabilityAndStatusAvailable() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setStatus(null);
        touristPackage.setAvailableSlots(4);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));
        when(touristPackageRepository.save(any(TouristPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        TouristPackageEntity reserved = touristPackageService.reservePackageSlot(1L);

        // THEN
        assertEquals(3, reserved.getAvailableSlots());
        assertTrue(reserved.getAvailable());
        assertEquals("AVAILABLE", reserved.getStatus());
    }

    @Test
    void reservePackageSlot_givenDirtyLists_whenReserved_thenKeepsNormalizedListsFromRead() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setDestinations(null);
        touristPackage.setActivities(null);
        touristPackage.setExtraServices(null);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));
        when(touristPackageRepository.save(any(TouristPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        TouristPackageEntity reserved = touristPackageService.reservePackageSlot(1L);

        // THEN
        assertEquals(List.of(), reserved.getDestinations());
        assertEquals(List.of(), reserved.getActivities());
        assertEquals(List.of(), reserved.getExtraServices());
    }

    @Test
    void releasePackageSlot_givenPackageWithExistingSlots_whenReleased_thenIncrementsAndEnablesPackage() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setAvailableSlots(2);
        touristPackage.setAvailable(false);
        touristPackage.setStatus("UNAVAILABLE");
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));
        when(touristPackageRepository.save(any(TouristPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        TouristPackageEntity released = touristPackageService.releasePackageSlot(1L);

        // THEN
        assertEquals(3, released.getAvailableSlots());
        assertTrue(released.getAvailable());
    }

    @Test
    void releasePackageSlot_givenNullSlots_whenReleased_thenStartsFromOne() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setAvailableSlots(null);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));
        when(touristPackageRepository.save(any(TouristPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        TouristPackageEntity released = touristPackageService.releasePackageSlot(1L);

        // THEN
        assertEquals(1, released.getAvailableSlots());
    }

    @Test
    void releasePackageSlot_givenUnavailablePackage_whenReleased_thenSetsAvailableFlagTrue() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setAvailable(false);
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));
        when(touristPackageRepository.save(any(TouristPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        TouristPackageEntity released = touristPackageService.releasePackageSlot(1L);

        // THEN
        assertTrue(released.getAvailable());
    }

    @Test
    void releasePackageSlot_givenUnavailablePackage_whenReleased_thenSetsStatusAvailable() {
        // GIVEN
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(1L);
        touristPackage.setStatus("UNAVAILABLE");
        when(touristPackageRepository.findById(1L)).thenReturn(Optional.of(touristPackage));
        when(touristPackageRepository.save(any(TouristPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        TouristPackageEntity released = touristPackageService.releasePackageSlot(1L);

        // THEN
        assertEquals("AVAILABLE", released.getStatus());
    }

    @Test
    void releasePackageSlot_givenUnknownPackage_whenReleased_thenThrowsResourceNotFoundException() {
        // GIVEN
        when(touristPackageRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> touristPackageService.releasePackageSlot(999L));

        // THEN
        assertEquals("Paquete turistico no encontrado con ID: 999", exception.getMessage());
    }

    private TouristPackageEntity buildPackage() {
        TouristPackageEntity touristPackage = new TouristPackageEntity();
        touristPackage.setPackageName("Aventura");
        touristPackage.setDescription("Descripcion valida");
        touristPackage.setDestinations(List.of("Torres del Paine"));
        touristPackage.setActivities(List.of("Kayak"));
        touristPackage.setExtraServices(List.of("Seguro"));
        touristPackage.setDaysCount(3);
        touristPackage.setNightsCount(2);
        touristPackage.setRoomType("Suite");
        touristPackage.setTransferIncluded(true);
        touristPackage.setAutomobileServiceIncluded(false);
        touristPackage.setPrice(250000.0);
        touristPackage.setAvailableSlots(3);
        touristPackage.setStatus("AVAILABLE");
        touristPackage.setAvailable(true);
        touristPackage.setMaxGuests(4);
        touristPackage.setAvailableFrom(LocalDate.of(2026, 4, 20));
        touristPackage.setAvailableUntil(LocalDate.of(2026, 4, 23));
        return touristPackage;
    }
}
