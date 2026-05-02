package kartingRM.Backend.services;

import kartingRM.Backend.Entities.TouristPackageEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.ReservationRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TouristPackageServiceTest {

    @Mock
    private TouristPackageRepository touristPackageRepository;

    @Mock
    private ReservationRepository reservationRepository;

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
        touristPackage.setAvailableFrom(LocalDate.now().plusDays(20));
        touristPackage.setAvailableUntil(LocalDate.now().plusDays(19));

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

    @Test
    void searchAvailablePackages_givenFilters_thenReturnsOnlyMatchingPackages() {
        TouristPackageEntity matching = buildPackage();
        matching.setPackageName("Atacama premium");
        matching.setDestinations(List.of("San Pedro de Atacama"));
        matching.setPrice(250000.0);
        matching.setDaysCount(3);
        matching.setTravelType("AVENTURA");
        TouristPackageEntity expensive = buildPackage();
        expensive.setPackageName("Europa");
        expensive.setDestinations(List.of("Madrid"));
        expensive.setPrice(2000000.0);
        expensive.setTravelType("CULTURAL");

        when(touristPackageRepository.findByAvailableTrueOrderByPackageNameAsc()).thenReturn(List.of(matching, expensive));

        List<TouristPackageEntity> result = touristPackageService.searchAvailablePackages(
                "Atacama",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                100000.0,
                400000.0,
                2,
                5,
                "AVENTURA"
        );

        assertEquals(1, result.size());
        assertEquals("Atacama premium", result.get(0).getPackageName());
    }

    @Test
    void createPackage_givenActivePromotionWithoutDates_thenRejects() {
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setPromotionActive(true);
        touristPackage.setPromotionDiscountPercent(10.0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> touristPackageService.createPackage(touristPackage));

        assertEquals("La promocion debe tener fecha de inicio y termino.", exception.getMessage());
    }

    @Test
    void deletePackage_givenAssociatedReservations_thenCancelsInsteadOfDeleting() {
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(50L);
        when(touristPackageRepository.findById(50L)).thenReturn(Optional.of(touristPackage));
        when(reservationRepository.existsByTouristPackageIdAndCancelledFalse(50L)).thenReturn(true);
        when(touristPackageRepository.save(any(TouristPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        touristPackageService.deletePackage(50L);

        assertFalse(touristPackage.getAvailable());
        assertEquals("CANCELLED", touristPackage.getStatus());
        verify(touristPackageRepository).save(touristPackage);
    }

    @Test
    void getAllPackages_givenNullOptionalFields_thenNormalizesForRead() {
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setDestinations(null);
        touristPackage.setActivities(null);
        touristPackage.setExtraServices(null);
        touristPackage.setAvailable(null);
        touristPackage.setTransferIncluded(null);
        touristPackage.setAutomobileServiceIncluded(null);
        touristPackage.setStatus(null);
        touristPackage.setTravelType(null);
        touristPackage.setSeason(null);
        touristPackage.setCategory(null);
        touristPackage.setPromotionActive(null);
        touristPackage.setPromotionDiscountPercent(null);
        when(touristPackageRepository.findAll()).thenReturn(List.of(touristPackage));

        List<TouristPackageEntity> result = touristPackageService.getAllPackages();

        TouristPackageEntity normalized = result.get(0);
        assertEquals(List.of(), normalized.getDestinations());
        assertFalse(normalized.getAvailable());
        assertEquals("UNAVAILABLE", normalized.getStatus());
        assertEquals("GENERAL", normalized.getTravelType());
        assertEquals("REGULAR", normalized.getSeason());
        assertEquals("STANDARD", normalized.getCategory());
        assertFalse(normalized.getPromotionActive());
        assertEquals(0.0, normalized.getPromotionDiscountPercent());
    }

    @Test
    void getAvailablePackages_givenExpiredOrUnavailableRows_thenReturnsOnlyPublicBookableRows() {
        TouristPackageEntity active = buildPackage();
        active.setPackageName("Active");
        TouristPackageEntity expired = buildPackage();
        expired.setPackageName("Expired");
        expired.setAvailableUntil(LocalDate.now().minusDays(1));
        when(touristPackageRepository.findByAvailableTrueOrderByPackageNameAsc()).thenReturn(List.of(active, expired));

        List<TouristPackageEntity> result = touristPackageService.getAvailablePackages();

        assertEquals(1, result.size());
        assertEquals("Active", result.get(0).getPackageName());
    }

    @Test
    void reservePackageSlots_givenInvalidRequestedSlots_thenRejects() {
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(60L);
        when(touristPackageRepository.findById(60L)).thenReturn(Optional.of(touristPackage));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> touristPackageService.reservePackageSlots(60L, 0));

        assertEquals("La cantidad de cupos solicitada debe ser mayor a cero.", exception.getMessage());
    }

    @Test
    void reservePackageSlots_givenRequestedSlotsAboveAvailability_thenRejects() {
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(61L);
        touristPackage.setAvailableSlots(2);
        when(touristPackageRepository.findById(61L)).thenReturn(Optional.of(touristPackage));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> touristPackageService.reservePackageSlots(61L, 3));

        assertEquals("La cantidad solicitada excede los cupos disponibles del paquete.", exception.getMessage());
    }

    @Test
    void reservePackageSlots_givenExpiredPackage_thenRejectsAsNotCurrent() {
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(62L);
        touristPackage.setAvailableFrom(LocalDate.now().minusDays(5));
        touristPackage.setAvailableUntil(LocalDate.now().minusDays(1));
        when(touristPackageRepository.findById(62L)).thenReturn(Optional.of(touristPackage));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> touristPackageService.reservePackageSlots(62L, 1));

        assertEquals("El paquete turistico no se encuentra vigente para clientes.", exception.getMessage());
    }

    @Test
    void updatePackage_givenPayload_thenCopiesNewFieldsAndNormalizes() {
        TouristPackageEntity existing = buildPackage();
        existing.setId(70L);
        TouristPackageEntity payload = buildPackage();
        payload.setPackageName("  Norte cultural ");
        payload.setDescription("Nueva descripcion");
        payload.setTravelType(" cultural ");
        payload.setSeason(" alta ");
        payload.setCategory(" premium ");
        payload.setPromotionActive(true);
        payload.setPromotionDiscountPercent(15.0);
        payload.setPromotionStartDate(LocalDate.now().minusDays(1));
        payload.setPromotionEndDate(LocalDate.now().plusDays(1));

        when(touristPackageRepository.findById(70L)).thenReturn(Optional.of(existing));
        when(touristPackageRepository.save(any(TouristPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TouristPackageEntity updated = touristPackageService.updatePackage(70L, payload);

        assertEquals("Norte cultural", updated.getPackageName());
        assertEquals("CULTURAL", updated.getTravelType());
        assertEquals("ALTA", updated.getSeason());
        assertEquals("PREMIUM", updated.getCategory());
        assertTrue(updated.getPromotionActive());
        assertEquals(15.0, updated.getPromotionDiscountPercent());
    }

    @Test
    void updateAvailability_givenFalse_thenStoresUnavailableState() {
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(71L);
        when(touristPackageRepository.findById(71L)).thenReturn(Optional.of(touristPackage));
        when(touristPackageRepository.save(any(TouristPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TouristPackageEntity updated = touristPackageService.updateAvailability(71L, false);

        assertFalse(updated.getAvailable());
        assertEquals("UNAVAILABLE", updated.getStatus());
    }

    @Test
    void deletePackage_givenNoAssociatedReservations_thenDeletesPhysically() {
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setId(72L);
        when(touristPackageRepository.findById(72L)).thenReturn(Optional.of(touristPackage));
        when(reservationRepository.existsByTouristPackageIdAndCancelledFalse(72L)).thenReturn(false);

        touristPackageService.deletePackage(72L);

        verify(touristPackageRepository).delete(touristPackage);
        verify(touristPackageRepository, never()).save(touristPackage);
    }

    @Test
    void createPackage_givenPromotionEndBeforeStart_thenRejects() {
        TouristPackageEntity touristPackage = buildPackage();
        touristPackage.setPromotionActive(true);
        touristPackage.setPromotionDiscountPercent(10.0);
        touristPackage.setPromotionStartDate(LocalDate.now().plusDays(2));
        touristPackage.setPromotionEndDate(LocalDate.now().plusDays(1));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> touristPackageService.createPackage(touristPackage));

        assertEquals("La fecha termino de promocion no puede ser anterior a la fecha inicio.", exception.getMessage());
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
        touristPackage.setAvailableFrom(LocalDate.now().plusDays(20));
        touristPackage.setAvailableUntil(LocalDate.now().plusDays(23));
        return touristPackage;
    }
}
