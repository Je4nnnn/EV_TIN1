package kartingRM.Backend.services;

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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(20), LocalDate.now().plusDays(22), "Noche", "Simple", 1L);
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
        LocalDate birthdayReservationDate = LocalDate.of(LocalDate.now().getYear() + 1, 4, 14);
        ReservationEntity reservation = buildReservation(birthdayReservationDate, birthdayReservationDate.plusDays(1), "Noche", "Simple", 1L);
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
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(30), LocalDate.now().plusDays(31), "Noche", "Simple", 1L);
        reservation.setTouristPackageId(9L);
        reservation.setTouristPackageName("  ");
        TouristPackageEntity touristPackage = new TouristPackageEntity();
        touristPackage.setId(9L);
        touristPackage.setPackageName("Patagonia");
        LocalDate packageCheckIn = LocalDate.now().plusDays(60);
        LocalDate packageCheckOut = LocalDate.now().plusDays(63);
        touristPackage.setAvailableFrom(packageCheckIn);
        touristPackage.setAvailableUntil(packageCheckOut);
        touristPackage.setRoomType("Suite");
        touristPackage.setPrice(250000.0);
        touristPackage.setAvailableSlots(4);
        touristPackage.setAvailable(true);
        touristPackage.setStatus("AVAILABLE");
        touristPackage.setMaxGuests(4);
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.of(1990, 1, 1));
        RoomEntity room = buildRoom(1L, "SU001", "Suite", "AVAILABLE");

        when(touristPackageService.getPackageById(9L)).thenReturn(touristPackage);
        when(touristPackageService.isPubliclyBookable(eq(touristPackage), any(LocalDate.class))).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser(2L, "22-2", LocalDate.of(1992, 2, 2))));
        when(reservationRepository.findByClienteRutIgnoreCaseAndCancelledFalse("11-1")).thenReturn(List.of());
        when(roomService.getAvailableRoomById(1L, touristPackage.getAvailableFrom(), touristPackage.getAvailableUntil(), "Suite", "Completo"))
                .thenReturn(room);
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        ReservationEntity saved = reservationService.saveReservation(reservation);

        // THEN
        assertEquals("Patagonia", saved.getTouristPackageName());
        assertEquals(packageCheckIn, saved.getCheckInDate());
        assertEquals(packageCheckOut, saved.getCheckOutDate());
        assertEquals("Suite", saved.getRoomType());
        assertEquals("Completo", saved.getStayType());
        verify(touristPackageService).reservePackageSlots(9L, 2);
    }

    @Test
    void saveReservation_givenEmptyDetails_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        ReservationEntity reservation = new ReservationEntity();
        reservation.setCheckInDate(LocalDate.now().plusDays(20));
        reservation.setCheckOutDate(LocalDate.now().plusDays(21));
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
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(20), LocalDate.now().plusDays(21), "Noche", "Simple", 1L);
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
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser(2L, "22-2", LocalDate.of(1992, 2, 2))));
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
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(30), LocalDate.now().plusDays(31), "Noche", "Simple", 1L);
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
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(20), LocalDate.now().plusDays(21), "Noche", "Simple", 1L);
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
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(20), LocalDate.now().plusDays(20), "Noche", "Simple", 1L);
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
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(20), LocalDate.now().plusDays(21), "Noche", "Simple", 1L);
        reservation.setDetails(List.of(buildDetail(1L, "Ana"), buildDetail(1L, "Ana 2")));
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.of(1990, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("No se puede repetir el mismo huesped dentro de una reserva.", exception.getMessage());
        verify(reservationRepository, never()).save(any(ReservationEntity.class));
    }

    @Test
    void saveReservation_givenRoomServiceRejectsRoom_whenSaved_thenPropagatesBusinessException() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(20), LocalDate.now().plusDays(22), "Noche", "Simple", 1L);
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.of(1990, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser(2L, "22-2", LocalDate.of(1992, 2, 2))));
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
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(20), LocalDate.now().plusDays(21), "Noche", "Simple", 1L);
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
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(20), LocalDate.now().plusDays(21), "   ", "Simple", 1L);

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("Debe especificar el tipo de estancia.", exception.getMessage());
    }

    @Test
    void saveReservation_givenDetailWithoutUserId_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(20), LocalDate.now().plusDays(21), "Noche", "Simple", 1L);
        reservation.setDetails(List.of(buildDetail(null, "Invitado"), buildDetail(2L, "Beto")));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("Cada detalle debe incluir un userId valido.", exception.getMessage());
    }

    @Test
    void saveReservation_givenMainGuestUnderage_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(10), LocalDate.now().plusDays(11), "Noche", "Simple", 1L);
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.now().minusYears(17));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser(2L, "22-2", LocalDate.of(1992, 2, 2))));
        doNothing().when(userService).validateReservationGuestAge(any(UserEntity.class), any(LocalDate.class), eq(false));
        doThrow(new BusinessException("El huesped principal debe ser mayor de edad para realizar la reserva."))
                .when(userService).validateReservationGuestAge(eq(mainClient), any(LocalDate.class), eq(true));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("El huesped principal debe ser mayor de edad para realizar la reserva.", exception.getMessage());
    }

    @Test
    void saveReservation_givenGuestOlderThanOneHundred_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(10), LocalDate.now().plusDays(11), "Noche", "Simple", 1L);
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.of(1990, 1, 1));
        UserEntity oldGuest = buildUser(2L, "22-2", LocalDate.now().minusYears(101));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(oldGuest));
        doNothing().when(userService).validateReservationGuestAge(eq(mainClient), any(LocalDate.class), eq(false));
        doThrow(new BusinessException("No se permiten reservas con huespedes mayores de 100 anos."))
                .when(userService).validateReservationGuestAge(eq(oldGuest), any(LocalDate.class), eq(false));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("No se permiten reservas con huespedes mayores de 100 anos.", exception.getMessage());
    }

    @Test
    void saveReservation_givenMainGuestMissingFromDetails_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(10), LocalDate.now().plusDays(11), "Noche", "Simple", 1L);
        reservation.setCliente(new UserEntity());
        reservation.getCliente().setId(3L);

        when(userRepository.findById(3L)).thenReturn(Optional.of(buildUser(3L, "33-3", LocalDate.of(1990, 1, 1))));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L, "11-1", LocalDate.of(1991, 1, 1))));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser(2L, "22-2", LocalDate.of(1992, 2, 2))));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("El huesped principal debe estar incluido entre los detalles de la reserva.", exception.getMessage());
    }

    @Test
    void saveReservation_givenPastCheckIn_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        ReservationEntity reservation = buildReservation(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "Noche", "Simple", 1L);

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.saveReservation(reservation));

        // THEN
        assertEquals("No se pueden registrar reservas con fecha de check-in en el pasado.", exception.getMessage());
    }

    @Test
    void confirmReservationPayment_givenPendingReservationAndFullAmount_thenMarksConfirmed() {
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4), "Noche", "Simple", 1L);
        reservation.setId(30L);
        reservation.setFinalAmount(120000.0);
        reservation.setStatus("PENDING_PAYMENT");
        reservation.setCancelled(false);
        when(reservationRepository.findById(30L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationEntity result = reservationService.confirmReservationPayment(30L, 120000.0);

        assertEquals("CONFIRMED", result.getStatus());
        assertEquals(120000.0, result.getAmountPaid());
        assertNotNull(result.getPaidAt());
    }

    @Test
    void confirmReservationPayment_givenPartialAmount_thenRejects() {
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4), "Noche", "Simple", 1L);
        reservation.setId(31L);
        reservation.setFinalAmount(120000.0);
        reservation.setStatus("PENDING_PAYMENT");
        reservation.setCancelled(false);
        when(reservationRepository.findById(31L)).thenReturn(Optional.of(reservation));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.confirmReservationPayment(31L, 100000.0));

        assertEquals("El pago debe corresponder al monto total de la reserva.", exception.getMessage());
    }

    @Test
    void expireReservation_givenPendingPackageReservation_thenCancelsAndReleasesSlots() {
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4), "Noche", "Simple", 1L);
        reservation.setId(32L);
        reservation.setStatus("PENDING_PAYMENT");
        reservation.setTouristPackageId(9L);
        reservation.setNumberOfGuests(2);
        reservation.setCancelled(false);
        when(reservationRepository.findById(32L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationEntity result = reservationService.expireReservation(32L);

        assertEquals("EXPIRED", result.getStatus());
        assertTrue(result.getCancelled());
        verify(touristPackageService, times(2)).releasePackageSlot(9L);
    }

    @Test
    void getSalesReport_givenPeriod_thenReturnsChronologicalRowsWithoutCancelledReservations() {
        ReservationEntity paid = reportReservation(1L, "CONFIRMED", false, "Atacama", 3, 300000.0, 300000.0);
        ReservationEntity cancelled = reportReservation(2L, "CANCELLED", true, "Patagonia", 2, 200000.0, 0.0);
        when(reservationRepository.findByCancelledFalse()).thenReturn(List.of(paid));

        var rows = reservationService.getSalesReport(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertEquals(1, rows.size());
        assertEquals("Atacama", rows.get(0).packageName());
        assertEquals(3, rows.get(0).passengerCount());
        assertEquals(300000.0, rows.get(0).paidAmount());
        assertFalse(rows.stream().anyMatch(row -> row.packageName().equals(cancelled.getTouristPackageName())));
    }

    @Test
    void getPackageRankingReport_givenReservations_thenGroupsAndSortsByPassengers() {
        ReservationEntity atacama = reportReservation(1L, "CONFIRMED", false, "Atacama", 3, 300000.0, 300000.0);
        atacama.setTouristPackageId(10L);
        ReservationEntity patagonia = reportReservation(2L, "PENDING_PAYMENT", false, "Patagonia", 5, 500000.0, 0.0);
        patagonia.setTouristPackageId(11L);
        when(reservationRepository.findByCancelledFalse()).thenReturn(List.of(atacama, patagonia));

        var ranking = reservationService.getPackageRankingReport(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertEquals(2, ranking.size());
        assertEquals("Patagonia", ranking.get(0).packageName());
        assertEquals(5L, ranking.get(0).passengerCount());
    }

    @Test
    void updateReservation_givenValidPayload_thenCopiesFieldsAndPersists() {
        ReservationEntity existing = buildReservation(LocalDate.now().plusDays(10), LocalDate.now().plusDays(11), "Noche", "Simple", 1L);
        existing.setId(40L);
        existing.setDetails(new java.util.ArrayList<>(existing.getDetails()));
        ReservationEntity payload = buildReservation(LocalDate.now().plusDays(20), LocalDate.now().plusDays(22), "Completo", "Double", 2L);
        UserEntity mainClient = buildUser(1L, "11-1", LocalDate.of(1990, 1, 1));
        RoomEntity room = buildRoom(2L, "D001", "Double", "AVAILABLE");

        when(reservationRepository.findById(40L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mainClient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser(2L, "22-2", LocalDate.of(1992, 2, 2))));
        when(reservationRepository.findByClienteRutIgnoreCaseAndCancelledFalse("11-1"))
                .thenReturn(List.of(existingReservation(40L, LocalDate.now().plusDays(1), "Noche")));
        when(roomService.getAvailableRoomById(2L, payload.getCheckInDate(), payload.getCheckOutDate(), "Double", "Completo"))
                .thenReturn(room);
        when(userService.obtenerDescuentoPorCategoria(any(Long.class))).thenReturn(0.0);
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationEntity updated = reservationService.updateReservation(40L, payload);

        assertEquals("D001", updated.getRoomNumber());
        assertEquals("Double", updated.getRoomType());
        assertEquals(2, updated.getNumberOfGuests());
        assertEquals(448000, updated.getFinalAmount());
    }

    @Test
    void deleteReservation_givenPackageReservation_thenMarksCancelledAndReleasesSlots() {
        ReservationEntity reservation = buildReservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6), "Noche", "Simple", 1L);
        reservation.setId(41L);
        reservation.setTouristPackageId(9L);
        reservation.setNumberOfGuests(3);
        reservation.setCancelled(false);

        when(reservationRepository.findById(41L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reservationService.deleteReservation(41L);

        assertTrue(reservation.getCancelled());
        assertEquals("CANCELLED", reservation.getStatus());
        verify(touristPackageService, times(3)).releasePackageSlot(9L);
    }

    @Test
    void getAllReservations_givenRepositoryRows_thenInitializesAndReturnsOnlyActiveRows() {
        ReservationEntity reservation = reportReservation(50L, "CONFIRMED", false, "Atacama", 2, 200000.0, 200000.0);
        when(reservationRepository.findByCancelledFalse()).thenReturn(List.of(reservation));

        List<ReservationEntity> result = reservationService.getAllReservations();

        assertEquals(1, result.size());
        assertEquals(50L, result.get(0).getId());
    }

    @Test
    void getReporteIngresosPorVueltasOTiempo_givenReservations_thenAggregatesByRoomAndMonth() {
        ReservationEntity simple = reportReservation(60L, "CONFIRMED", false, "Sin paquete", 2, 180000.0, 180000.0);
        simple.setTouristPackageId(null);
        simple.setTouristPackageName(null);
        simple.setRoomType("Simple");
        simple.setCheckInDate(LocalDate.of(LocalDate.now().getYear(), 5, 10));
        simple.setCreatedAt(simple.getCheckInDate().atStartOfDay());
        simple.setPaidAt(simple.getCheckInDate().atStartOfDay());
        when(reservationRepository.findByCancelledFalse()).thenReturn(List.of(simple));

        var report = reservationService.getReporteIngresosPorVueltasOTiempo(simple.getCheckInDate(), simple.getCheckInDate());

        assertEquals(180000.0, report.get("Simple").get("Mayo"));
        assertEquals(180000.0, report.get("TOTAL").get("TOTAL"));
    }

    @Test
    void getReporteIngresosPorCantidadDePersonas_givenReservations_thenAggregatesByRangeAndMonth() {
        ReservationEntity reservation = reportReservation(61L, "CONFIRMED", false, "Sin paquete", 3, 270000.0, 270000.0);
        reservation.setTouristPackageId(null);
        reservation.setTouristPackageName(null);
        reservation.setCheckInDate(LocalDate.of(LocalDate.now().getYear(), 6, 10));
        reservation.setCreatedAt(reservation.getCheckInDate().atStartOfDay());
        reservation.setPaidAt(reservation.getCheckInDate().atStartOfDay());
        reservation.setDetails(List.of(amountDetail(1L, "Ana", 90000.0), amountDetail(2L, "Beto", 90000.0), amountDetail(3L, "Carla", 90000.0)));
        when(reservationRepository.findByCancelledFalse()).thenReturn(List.of(reservation));

        var report = reservationService.getReporteIngresosPorCantidadDePersonas(reservation.getCheckInDate(), reservation.getCheckInDate());

        assertEquals(270000.0, report.get("3-5 personas").get("Junio"));
        assertEquals(270000.0, report.get("TOTAL").get("TOTAL"));
    }

    @Test
    void getSalesReport_givenInvalidDateRange_thenThrowsBusinessException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.getSalesReport(LocalDate.now().plusDays(1), LocalDate.now()));

        assertEquals("La fecha fin no puede ser anterior a la fecha inicio.", exception.getMessage());
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

    private ReservationEntity reportReservation(
            Long id,
            String status,
            boolean cancelled,
            String packageName,
            int passengers,
            double total,
            double paid
    ) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(id);
        reservation.setStatus(status);
        reservation.setCancelled(cancelled);
        reservation.setCreatedAt(java.time.LocalDateTime.now());
        reservation.setPaidAt(paid > 0 ? java.time.LocalDateTime.now() : null);
        reservation.setTouristPackageId(id + 100);
        reservation.setTouristPackageName(packageName);
        reservation.setNumberOfGuests(passengers);
        reservation.setFinalAmount(total);
        reservation.setAmountPaid(paid);
        reservation.setCheckInDate(LocalDate.now().plusDays(30));
        reservation.setCliente(buildUser(id, id + "-1", LocalDate.of(1990, 1, 1)));
        reservation.setDetails(List.of(buildDetail(id, "Cliente " + id)));
        return reservation;
    }

    private ReservationDetailsEntity buildDetail(Long userId, String guestName) {
        ReservationDetailsEntity detail = new ReservationDetailsEntity();
        detail.setUserId(userId);
        detail.setGuestName(guestName);
        return detail;
    }

    private ReservationDetailsEntity amountDetail(Long userId, String guestName, double finalAmount) {
        ReservationDetailsEntity detail = buildDetail(userId, guestName);
        detail.setFinalAmount(finalAmount);
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
