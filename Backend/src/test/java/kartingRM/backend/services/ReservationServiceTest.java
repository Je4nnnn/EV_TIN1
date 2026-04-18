package kartingRM.backend.services;

import kartingRM.Backend.Entities.ReservationDetailsEntity;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Entities.RoomEntity;
import kartingRM.Backend.Entities.TouristPackageEntity;
import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Repositories.ReservationRepository;
import kartingRM.Backend.Repositories.UserRepository;
import kartingRM.Backend.Services.ReservationService;
import kartingRM.Backend.Services.RoomService;
import kartingRM.Backend.Services.TouristPackageService;
import kartingRM.Backend.Services.UserService;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TouristPackageService touristPackageService;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void esCumpleanos_givenSameDayAndMonth_whenEvaluated_thenReturnsTrue() {
        // GIVEN
        LocalDate birthday = LocalDate.of(1995, 4, 14);
        LocalDate checkIn = LocalDate.of(2026, 4, 14);

        // WHEN
        boolean result = reservationService.esCumpleanos(birthday, checkIn);

        // THEN
        assertTrue(result);
    }

    @Test
    void esCumpleanos_givenDifferentDay_whenEvaluated_thenReturnsFalse() {
        // GIVEN
        LocalDate birthday = LocalDate.of(1995, 4, 13);
        LocalDate checkIn = LocalDate.of(2026, 4, 14);

        // WHEN
        boolean result = reservationService.esCumpleanos(birthday, checkIn);

        // THEN
        assertFalse(result);
    }

    @Test
    void esCumpleanos_givenDifferentMonth_whenEvaluated_thenReturnsFalse() {
        // GIVEN
        LocalDate birthday = LocalDate.of(1995, 5, 14);
        LocalDate checkIn = LocalDate.of(2026, 4, 14);

        // WHEN
        boolean result = reservationService.esCumpleanos(birthday, checkIn);

        // THEN
        assertFalse(result);
    }

    @Test
    void esCumpleanos_givenNullBirthday_whenEvaluated_thenReturnsFalse() {
        // GIVEN
        LocalDate checkIn = LocalDate.of(2026, 4, 14);

        // WHEN
        boolean result = reservationService.esCumpleanos(null, checkIn);

        // THEN
        assertFalse(result);
    }

    @Test
    void esCumpleanos_givenNullCheckIn_whenEvaluated_thenReturnsFalse() {
        // GIVEN
        LocalDate birthday = LocalDate.of(1995, 4, 14);

        // WHEN
        boolean result = reservationService.esCumpleanos(birthday, null);

        // THEN
        assertFalse(result);
    }

    @Test
    void calcularNumeroDias_givenCheckInAndCheckOutEqual_whenCalculated_thenReturnsOne() {
        // GIVEN
        ReservationEntity reservation = new ReservationEntity();
        reservation.setCheckInDate(LocalDate.of(2026, 4, 14));
        reservation.setCheckOutDate(LocalDate.of(2026, 4, 14));

        // WHEN
        long result = reservationService.calcularNumeroDias(reservation);

        // THEN
        assertEquals(1, result);
    }

    @Test
    void calcularNumeroDias_givenSeveralDays_whenCalculated_thenReturnsDifference() {
        // GIVEN
        ReservationEntity reservation = new ReservationEntity();
        reservation.setCheckInDate(LocalDate.of(2026, 4, 14));
        reservation.setCheckOutDate(LocalDate.of(2026, 4, 18));

        // WHEN
        long result = reservationService.calcularNumeroDias(reservation);

        // THEN
        assertEquals(4, result);
    }

    @Test
    void calcularNumeroDias_givenNegativeDifference_whenCalculated_thenReturnsOne() {
        // GIVEN
        ReservationEntity reservation = new ReservationEntity();
        reservation.setCheckInDate(LocalDate.of(2026, 4, 18));
        reservation.setCheckOutDate(LocalDate.of(2026, 4, 14));

        // WHEN
        long result = reservationService.calcularNumeroDias(reservation);

        // THEN
        assertEquals(1, result);
    }

    @Test
    void calcularNumeroDias_givenNullCheckIn_whenCalculated_thenReturnsOne() {
        // GIVEN
        ReservationEntity reservation = new ReservationEntity();
        reservation.setCheckOutDate(LocalDate.of(2026, 4, 14));

        // WHEN
        long result = reservationService.calcularNumeroDias(reservation);

        // THEN
        assertEquals(1, result);
    }

    @Test
    void calcularNumeroDias_givenNullCheckOut_whenCalculated_thenReturnsOne() {
        // GIVEN
        ReservationEntity reservation = new ReservationEntity();
        reservation.setCheckInDate(LocalDate.of(2026, 4, 14));

        // WHEN
        long result = reservationService.calcularNumeroDias(reservation);

        // THEN
        assertEquals(1, result);
    }

    @Test
    void calcularMaxCumpleanos_givenTwoGuests_whenCalculated_thenReturnsZero() {
        // GIVEN
        int guestCount = 2;

        // WHEN
        int result = reservationService.calcularMaxCumpleanos(guestCount);

        // THEN
        assertEquals(0, result);
    }

    @Test
    void calcularMaxCumpleanos_givenThreeGuests_whenCalculated_thenReturnsOne() {
        // GIVEN
        int guestCount = 3;

        // WHEN
        int result = reservationService.calcularMaxCumpleanos(guestCount);

        // THEN
        assertEquals(1, result);
    }

    @Test
    void calcularMaxCumpleanos_givenFiveGuests_whenCalculated_thenReturnsOne() {
        // GIVEN
        int guestCount = 5;

        // WHEN
        int result = reservationService.calcularMaxCumpleanos(guestCount);

        // THEN
        assertEquals(1, result);
    }

    @Test
    void calcularMaxCumpleanos_givenSixGuests_whenCalculated_thenReturnsTwo() {
        // GIVEN
        int guestCount = 6;

        // WHEN
        int result = reservationService.calcularMaxCumpleanos(guestCount);

        // THEN
        assertEquals(2, result);
    }

    @Test
    void calcularMaxCumpleanos_givenNegativeGuests_whenCalculated_thenReturnsZero() {
        // GIVEN
        int guestCount = -4;

        // WHEN
        int result = reservationService.calcularMaxCumpleanos(guestCount);

        // THEN
        assertEquals(0, result);
    }

    @Test
    void calcularTarifaBase_givenSimpleNight_whenCalculated_thenReturnsStandardRate() {
        // GIVEN
        String roomType = "Simple";
        String stayType = "Noche";

        // WHEN
        double result = reservationService.calcularTarifaBase(roomType, stayType);

        // THEN
        assertEquals(50000, result);
    }

    @Test
    void calcularTarifaBase_givenDoubleMorning_whenCalculated_thenReturnsReducedRate() {
        // GIVEN
        String roomType = "Double";
        String stayType = "Mañana";

        // WHEN
        double result = reservationService.calcularTarifaBase(roomType, stayType);

        // THEN
        assertEquals(48000, result);
    }

    @Test
    void calcularTarifaBase_givenSuiteComplete_whenCalculated_thenReturnsFullRate() {
        // GIVEN
        String roomType = "Suite";
        String stayType = "Completo";

        // WHEN
        double result = reservationService.calcularTarifaBase(roomType, stayType);

        // THEN
        assertEquals(210000, result);
    }

    @Test
    void calcularTarifaBase_givenBlankInputs_whenCalculated_thenUsesDefaults() {
        // GIVEN
        String roomType = "   ";
        String stayType = "";

        // WHEN
        double result = reservationService.calcularTarifaBase(roomType, stayType);

        // THEN
        assertEquals(50000, result);
    }

    @Test
    void calcularTarifaBase_givenUnknownRoomType_whenCalculated_thenFallsBackToSimpleRate() {
        // GIVEN
        String roomType = "Presidencial";
        String stayType = "Noche";

        // WHEN
        double result = reservationService.calcularTarifaBase(roomType, stayType);

        // THEN
        assertEquals(50000, result);
    }

    @Test
    void calcularDescuentoGrupo_givenTwoGuests_whenCalculated_thenReturnsZero() {
        // GIVEN
        int guestCount = 2;

        // WHEN
        double result = reservationService.calcularDescuentoGrupo(guestCount);

        // THEN
        assertEquals(0.0, result);
    }

    @Test
    void calcularDescuentoGrupo_givenThreeGuests_whenCalculated_thenReturnsTenPercent() {
        // GIVEN
        int guestCount = 3;

        // WHEN
        double result = reservationService.calcularDescuentoGrupo(guestCount);

        // THEN
        assertEquals(0.10, result);
    }

    @Test
    void calcularDescuentoGrupo_givenSixGuests_whenCalculated_thenReturnsTwentyPercent() {
        // GIVEN
        int guestCount = 6;

        // WHEN
        double result = reservationService.calcularDescuentoGrupo(guestCount);

        // THEN
        assertEquals(0.20, result);
    }

    @Test
    void calcularDescuentoGrupo_givenElevenGuests_whenCalculated_thenReturnsThirtyPercent() {
        // GIVEN
        int guestCount = 11;

        // WHEN
        double result = reservationService.calcularDescuentoGrupo(guestCount);

        // THEN
        assertEquals(0.30, result);
    }

    @Test
    void calcularDescuentoGrupo_givenNegativeGuests_whenCalculated_thenReturnsZero() {
        // GIVEN
        int guestCount = -2;

        // WHEN
        double result = reservationService.calcularDescuentoGrupo(guestCount);

        // THEN
        assertEquals(0.0, result);
    }

    @Test
    void saveReservation_givenValidReservation_whenSaved_thenCalculatesAmountsAndPersists() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 22), "Noche", "Simple", 1L);
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.of(1990, 1, 1));
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser(2L, "22-2", LocalDate.of(1992, 2, 2))));
        when(reservationRepository.findByClienteRutIgnoreCaseAndCancelledFalse("11-1")).thenReturn(List.of());
        when(roomService.getAvailableRoomById(1L, reservation.getCheckInDate(), reservation.getCheckOutDate(), "Simple", "Noche"))
                .thenReturn(room);
        when(userService.obtenerDescuentoPorCategoria(1L)).thenReturn(0.05);
        when(userService.obtenerDescuentoPorCategoria(2L)).thenReturn(0.10);
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        ReservationEntity saved = reservationService.saveReservation(reservation);

        // THEN
        assertEquals("S001", saved.getRoomNumber());
        assertEquals("Simple", saved.getRoomType());
        assertEquals(95000, saved.getDetails().get(0).getFinalAmount());
        assertEquals(90000, saved.getDetails().get(1).getFinalAmount());
        assertEquals(185000, saved.getFinalAmount());
        verify(userService).incrementVisitsAndUpdateCategory(1L);
        verify(userService).incrementVisitsAndUpdateCategory(2L);
    }

    @Test
    void saveReservation_givenBirthdayGuest_whenSaved_thenUsesHighestBirthdayDiscount() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.of(2026, 4, 14), LocalDate.of(2026, 4, 15), "Noche", "Simple", 1L);
        reservation.setDetails(List.of(buildDetail(1L, "Ana"), buildDetail(2L, "Beto"), buildDetail(3L, "Carla")));
        UserEntity birthdayUser = buildUser(1L, "11-1", LocalDate.of(1990, 4, 14));
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(birthdayUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser(2L, "22-2", LocalDate.of(1992, 5, 2))));
        when(userRepository.findById(3L)).thenReturn(Optional.of(buildUser(3L, "33-3", LocalDate.of(1993, 6, 3))));
        when(reservationRepository.findByClienteRutIgnoreCaseAndCancelledFalse("11-1")).thenReturn(List.of());
        when(roomService.getAvailableRoomById(1L, reservation.getCheckInDate(), reservation.getCheckOutDate(), "Simple", "Noche"))
                .thenReturn(room);
        when(userService.obtenerDescuentoPorCategoria(1L)).thenReturn(0.20);
        when(userService.obtenerDescuentoPorCategoria(2L)).thenReturn(0.0);
        when(userService.obtenerDescuentoPorCategoria(3L)).thenReturn(0.0);
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        ReservationEntity saved = reservationService.saveReservation(reservation);

        // THEN
        assertEquals(0.50, saved.getDetails().get(0).getDiscount());
        assertEquals(25000, saved.getDetails().get(0).getFinalAmount());
    }

    @Test
    void saveReservation_givenTouristPackage_whenSaved_thenUsesPackageDataAndConsumesSlot() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2), "Noche", "Simple", 1L);
        reservation.setTouristPackageId(9L);
        reservation.setTouristPackageName("  ");
        TouristPackageEntity touristPackage = new TouristPackageEntity();
        touristPackage.setId(9L);
        touristPackage.setPackageName("Patagonia");
        touristPackage.setAvailableFrom(LocalDate.of(2026, 7, 1));
        touristPackage.setAvailableUntil(LocalDate.of(2026, 7, 4));
        touristPackage.setRoomType("Suite");
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.of(1990, 1, 1));
        RoomEntity room = buildRoom(1L, "SU001", "Suite", "AVAILABLE");

        when(touristPackageService.getPackageById(9L)).thenReturn(touristPackage);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser(2L, "22-2", LocalDate.of(1992, 2, 2))));
        when(reservationRepository.findByClienteRutIgnoreCaseAndCancelledFalse("11-1")).thenReturn(List.of());
        when(roomService.getAvailableRoomById(1L, touristPackage.getAvailableFrom(), touristPackage.getAvailableUntil(), "Suite", "Completo"))
                .thenReturn(room);
        when(userService.obtenerDescuentoPorCategoria(any(Long.class))).thenReturn(0.0);
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        ReservationEntity saved = reservationService.saveReservation(reservation);

        // THEN
        assertEquals("Patagonia", saved.getTouristPackageName());
        assertEquals(LocalDate.of(2026, 7, 1), saved.getCheckInDate());
        assertEquals(LocalDate.of(2026, 7, 4), saved.getCheckOutDate());
        assertEquals("Suite", saved.getRoomType());
        assertEquals("Completo", saved.getStayType());
        verify(touristPackageService).reservePackageSlot(9L);
    }

    @Test
    void saveReservation_givenEmptyDetails_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        ReservationEntity reservation = new ReservationEntity();
        reservation.setCheckInDate(LocalDate.of(2026, 4, 20));
        reservation.setCheckOutDate(LocalDate.of(2026, 4, 21));
        reservation.setStayType("Noche");
        reservation.setRoomType("Simple");
        reservation.setRoomId(1L);
        reservation.setDetails(List.of());

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("La reserva debe incluir al menos un detalle.", exception.getMessage());
    }

    @Test
    void saveReservation_givenMoreThanFifteenGuests_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Noche", "Simple", 1L);
        reservation.setDetails(List.of(
                buildDetail(1L, "Uno"), buildDetail(2L, "Dos"), buildDetail(3L, "Tres"), buildDetail(4L, "Cuatro"),
                buildDetail(5L, "Cinco"), buildDetail(6L, "Seis"), buildDetail(7L, "Siete"), buildDetail(8L, "Ocho"),
                buildDetail(9L, "Nueve"), buildDetail(10L, "Diez"), buildDetail(11L, "Once"), buildDetail(12L, "Doce"),
                buildDetail(13L, "Trece"), buildDetail(14L, "Catorce"), buildDetail(15L, "Quince"), buildDetail(16L, "Dieciseis")
        ));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("La cantidad de huespedes debe estar entre 1 y 15.", exception.getMessage());
    }

    @Test
    void saveReservation_givenClientWithThreeActiveReservations_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(7), "Noche", "Simple", 1L);
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.of(1990, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));
        when(reservationRepository.findByClienteRutIgnoreCaseAndCancelledFalse("11-1")).thenReturn(List.of(
                existingReservation(100L, LocalDate.now().plusDays(1), "Noche"),
                existingReservation(101L, LocalDate.now().plusDays(2), "Noche"),
                existingReservation(102L, LocalDate.now().plusDays(3), "Noche")
        ));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("El cliente con RUT 11-1 ya tiene 3 reservas activas.", exception.getMessage());
    }

    @Test
    void saveReservation_givenPackageWithoutAvailabilityWindow_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2), "Noche", "Simple", 1L);
        reservation.setTouristPackageId(9L);
        TouristPackageEntity touristPackage = new TouristPackageEntity();
        touristPackage.setId(9L);
        touristPackage.setPackageName("Patagonia");
        touristPackage.setRoomType("Suite");

        when(touristPackageService.getPackageById(9L)).thenReturn(touristPackage);

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("El paquete turistico seleccionado no tiene fechas configuradas.", exception.getMessage());
    }

    @Test
    void saveReservation_givenUnsortedGuestNames_whenSaved_thenSortsAndTrimsDetails() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Noche", "Simple", 1L);
        reservation.setDetails(List.of(buildDetail(2L, "  Zeta  "), buildDetail(1L, "  alfa ")));
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.of(1990, 1, 1));
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser(2L, "22-2", LocalDate.of(1992, 2, 2))));
        when(reservationRepository.findByClienteRutIgnoreCaseAndCancelledFalse("11-1")).thenReturn(List.of());
        when(roomService.getAvailableRoomById(1L, reservation.getCheckInDate(), reservation.getCheckOutDate(), "Simple", "Noche"))
                .thenReturn(room);
        when(userService.obtenerDescuentoPorCategoria(any(Long.class))).thenReturn(0.0);
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        ReservationEntity saved = reservationService.saveReservation(reservation);

        // THEN
        assertEquals("alfa", saved.getDetails().get(0).getGuestName());
        assertEquals("Zeta", saved.getDetails().get(1).getGuestName());
    }

    @Test
    void saveReservation_givenSameDayStay_whenSaved_thenUsesMinimumOneDayRate() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 20), "Noche", "Simple", 1L);
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.of(1990, 1, 1));
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser(2L, "22-2", LocalDate.of(1992, 2, 2))));
        when(reservationRepository.findByClienteRutIgnoreCaseAndCancelledFalse("11-1")).thenReturn(List.of());
        when(roomService.getAvailableRoomById(1L, reservation.getCheckInDate(), reservation.getCheckOutDate(), "Simple", "Noche"))
                .thenReturn(room);
        when(userService.obtenerDescuentoPorCategoria(any(Long.class))).thenReturn(0.0);
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        ReservationEntity saved = reservationService.saveReservation(reservation);

        // THEN
        assertEquals(100000, saved.getFinalAmount());
    }

    @Test
    void saveReservation_givenRepeatedUserIds_whenSaved_thenIncrementsVisitsOnlyOncePerUser() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Noche", "Simple", 1L);
        reservation.setDetails(List.of(buildDetail(1L, "Ana"), buildDetail(1L, "Ana 2")));
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.of(1990, 1, 1));
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));
        when(reservationRepository.findByClienteRutIgnoreCaseAndCancelledFalse("11-1")).thenReturn(List.of());
        when(roomService.getAvailableRoomById(1L, reservation.getCheckInDate(), reservation.getCheckOutDate(), "Simple", "Noche"))
                .thenReturn(room);
        when(userService.obtenerDescuentoPorCategoria(1L)).thenReturn(0.0);
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        reservationService.saveReservation(reservation);

        // THEN
        verify(userService).incrementVisitsAndUpdateCategory(1L);
    }

    @Test
    void saveReservation_givenRoomServiceRejectsRoom_whenSaved_thenPropagatesBusinessException() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 22), "Noche", "Simple", 1L);
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.of(1990, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));
        when(reservationRepository.findByClienteRutIgnoreCaseAndCancelledFalse("11-1")).thenReturn(List.of());
        when(roomService.getAvailableRoomById(eq(1L), any(LocalDate.class), any(LocalDate.class), eq("Simple"), eq("Noche")))
                .thenThrow(new BusinessException("La habitacion seleccionada ya no esta disponible para ese rango."));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("La habitacion seleccionada ya no esta disponible para ese rango.", exception.getMessage());
        verify(reservationRepository, never()).save(any(ReservationEntity.class));
    }

    @Test
    void saveReservation_givenNegativeGuestCountInPayload_whenSaved_thenUsesDetailCountSafely() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Noche", "Simple", 1L);
        reservation.setNumberOfGuests(-8);
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.of(1990, 1, 1));
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser(2L, "22-2", LocalDate.of(1992, 2, 2))));
        when(reservationRepository.findByClienteRutIgnoreCaseAndCancelledFalse("11-1")).thenReturn(List.of());
        when(roomService.getAvailableRoomById(1L, reservation.getCheckInDate(), reservation.getCheckOutDate(), "Simple", "Noche"))
                .thenReturn(room);
        when(userService.obtenerDescuentoPorCategoria(any(Long.class))).thenReturn(0.0);
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        ReservationEntity saved = reservationService.saveReservation(reservation);

        // THEN
        assertEquals(2, saved.getNumberOfGuests());
        assertEquals(100000, saved.getFinalAmount());
    }

    @Test
    void saveReservation_givenBlankStayType_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "   ", "Simple", 1L);

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("Debe especificar el tipo de estancia.", exception.getMessage());
    }

    @Test
    void saveReservation_givenDetailWithoutUserId_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Noche", "Simple", 1L);
        reservation.setDetails(List.of(buildDetail(null, "Invitado"), buildDetail(2L, "Beto")));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("Cada detalle debe incluir un userId valido.", exception.getMessage());
    }

    private ReservationEntity buildReservation(LocalDate checkIn, LocalDate checkOut, String stayType, String roomType, Long roomId) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setCheckInDate(checkIn);
        reservation.setCheckOutDate(checkOut);
        reservation.setStayType(stayType);
        reservation.setRoomType(roomType);
        reservation.setRoomId(roomId);
        UserEntity client = new UserEntity();
        client.setId(1L);
        reservation.setCliente(client);
        reservation.setDetails(List.of(buildDetail(1L, "Ana"), buildDetail(2L, "Beto")));
        return reservation;
    }

    private ReservationDetailsEntity buildDetail(Long userId, String guestName) {
        ReservationDetailsEntity detail = new ReservationDetailsEntity();
        detail.setUserId(userId);
        detail.setGuestName(guestName);
        return detail;
    }

    private UserEntity buildUser(Long id, String rut, LocalDate birthday) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setRut(rut);
        user.setName("Usuario " + id);
        user.setDateBirthday(birthday);
        user.setCategory_frecuency("Regular");
        user.setNumberVisits(2);
        return user;
    }

    private RoomEntity buildRoom(Long id, String roomNumber, String type, String status) {
        RoomEntity room = new RoomEntity();
        room.setId(id);
        room.setRoomNumber(roomNumber);
        room.setType(type);
        room.setStatus(status);
        return room;
    }

    private ReservationEntity existingReservation(Long id, LocalDate checkOutDate, String stayType) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(id);
        reservation.setCheckOutDate(checkOutDate);
        reservation.setStayType(stayType);
        reservation.setCancelled(false);
        return reservation;
    }
}
