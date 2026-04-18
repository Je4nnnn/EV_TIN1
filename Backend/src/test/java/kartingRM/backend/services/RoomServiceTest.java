package kartingRM.backend.services;

import kartingRM.Backend.DTOs.RoomAvailabilityResponse;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Entities.RoomEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.ReservationRepository;
import kartingRM.Backend.Repositories.RoomRepository;
import kartingRM.Backend.Services.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void saveRoom_givenValidRoom_whenSaved_thenTrimsAndPersists() {
        // GIVEN
        RoomEntity room = buildRoom(1L, " S001 ", " Simple ", " AVAILABLE ");
        when(roomRepository.findByRoomNumber("S001")).thenReturn(Optional.empty());
        when(roomRepository.save(room)).thenReturn(room);

        // WHEN
        RoomEntity saved = roomService.saveRoom(room);

        // THEN
        assertEquals("S001", saved.getRoomNumber());
        assertEquals("Simple", saved.getType());
        assertEquals("AVAILABLE", saved.getStatus());
    }

    @Test
    void saveRoom_givenNullRoom_whenSaved_thenThrowsBusinessException() {
        // GIVEN - WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.saveRoom(null));

        // THEN
        assertEquals("Debe enviar una habitacion valida.", exception.getMessage());
    }

    @Test
    void saveRoom_givenBlankNumber_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        RoomEntity room = buildRoom(1L, " ", "Simple", "AVAILABLE");

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.saveRoom(room));

        // THEN
        assertEquals("El numero de habitacion es obligatorio.", exception.getMessage());
    }

    @Test
    void saveRoom_givenBlankType_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        RoomEntity room = buildRoom(1L, "S001", " ", "AVAILABLE");

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.saveRoom(room));

        // THEN
        assertEquals("El tipo de habitacion es obligatorio.", exception.getMessage());
    }

    @Test
    void saveRoom_givenDuplicatedNumber_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");
        when(roomRepository.findByRoomNumber("S001")).thenReturn(Optional.of(buildRoom(2L, "S001", "Simple", "AVAILABLE")));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.saveRoom(room));

        // THEN
        assertEquals("Ya existe una habitacion con numero S001.", exception.getMessage());
    }

    @Test
    void getAvailableRoomById_givenAvailableRoom_whenRequested_thenReturnsRoom() {
        // GIVEN
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Simple")).thenReturn(List.of(room));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L))).thenReturn(List.of());

        // WHEN
        RoomEntity result = roomService.getAvailableRoomById(1L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Simple", "Noche");

        // THEN
        assertEquals("S001", result.getRoomNumber());
    }

    @Test
    void getAvailableRoomById_givenWrongType_whenRequested_thenThrowsBusinessException() {
        // GIVEN
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class,
                () -> roomService.getAvailableRoomById(1L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Suite", "Noche"));

        // THEN
        assertEquals("La habitacion seleccionada no corresponde al tipo solicitado.", exception.getMessage());
    }

    @Test
    void getAvailableRoomById_givenUnavailableRoom_whenRequested_thenThrowsBusinessException() {
        // GIVEN
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Simple")).thenReturn(List.of(room));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L))).thenReturn(List.of(
                reservation(1L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Noche")
        ));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class,
                () -> roomService.getAvailableRoomById(1L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Simple", "Noche"));

        // THEN
        assertEquals("La habitacion seleccionada ya no esta disponible para ese rango.", exception.getMessage());
    }

    @Test
    void getAvailableRoomById_givenMissingRoom_whenRequested_thenThrowsResourceNotFoundException() {
        // GIVEN
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> roomService.getAvailableRoomById(999L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Simple", "Noche"));

        // THEN
        assertEquals("Habitacion no encontrada con ID: 999", exception.getMessage());
    }

    @Test
    void getAvailableRoomById_givenCaseInsensitiveType_whenRequested_thenReturnsRoom() {
        // GIVEN
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("simple")).thenReturn(List.of(room));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L))).thenReturn(List.of());

        // WHEN
        RoomEntity result = roomService.getAvailableRoomById(1L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "simple", "Noche");

        // THEN
        assertEquals(1L, result.getId());
    }

    @Test
    void getAvailableRooms_givenRoomWithoutReservations_whenRequested_thenReturnsAvailableList() {
        // GIVEN
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Simple")).thenReturn(List.of(room));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L))).thenReturn(List.of());

        // WHEN
        List<RoomEntity> rooms = roomService.getAvailableRooms(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Simple", "Noche");

        // THEN
        assertEquals(1, rooms.size());
    }

    @Test
    void getAvailableRooms_givenOutOfServiceRoom_whenRequested_thenExcludesRoom() {
        // GIVEN
        RoomEntity room = buildRoom(1L, "S001", "Simple", "OUT_OF_SERVICE");
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Simple")).thenReturn(List.of(room));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L))).thenReturn(List.of());

        // WHEN
        List<RoomEntity> rooms = roomService.getAvailableRooms(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Simple", "Noche");

        // THEN
        assertTrue(rooms.isEmpty());
    }

    @Test
    void getAvailableRooms_givenNullCheckIn_whenRequested_thenThrowsBusinessException() {
        // GIVEN - WHEN
        BusinessException exception = assertThrows(BusinessException.class,
                () -> roomService.getAvailableRooms(null, LocalDate.of(2026, 4, 21), "Simple", "Noche"));

        // THEN
        assertEquals("Debe indicar check-in y check-out para consultar disponibilidad.", exception.getMessage());
    }

    @Test
    void getAvailableRooms_givenCheckOutBeforeCheckIn_whenRequested_thenThrowsBusinessException() {
        // GIVEN - WHEN
        BusinessException exception = assertThrows(BusinessException.class,
                () -> roomService.getAvailableRooms(LocalDate.of(2026, 4, 21), LocalDate.of(2026, 4, 20), "Simple", "Noche"));

        // THEN
        assertEquals("La fecha de check-out no puede ser anterior al check-in.", exception.getMessage());
    }

    @Test
    void getAvailableRooms_givenBlankRoomType_whenRequested_thenThrowsBusinessException() {
        // GIVEN - WHEN
        BusinessException exception = assertThrows(BusinessException.class,
                () -> roomService.getAvailableRooms(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), " ", "Noche"));

        // THEN
        assertEquals("Debe indicar el tipo de habitacion.", exception.getMessage());
    }

    @Test
    void getAvailableRooms_givenOverlappingReservation_whenRequested_thenExcludesRoom() {
        // GIVEN
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Simple")).thenReturn(List.of(room));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L))).thenReturn(List.of(
                reservation(1L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Noche")
        ));

        // WHEN
        List<RoomEntity> rooms = roomService.getAvailableRooms(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Simple", "Noche");

        // THEN
        assertTrue(rooms.isEmpty());
    }

    @Test
    void getAvailableRooms_givenNonOverlappingReservation_whenRequested_thenKeepsRoomAvailable() {
        // GIVEN
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Simple")).thenReturn(List.of(room));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L))).thenReturn(List.of(
                reservation(1L, LocalDate.of(2026, 4, 25), LocalDate.of(2026, 4, 26), "Noche")
        ));

        // WHEN
        List<RoomEntity> rooms = roomService.getAvailableRooms(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Simple", "Noche");

        // THEN
        assertEquals(1, rooms.size());
    }

    @Test
    void getAvailableRooms_givenBlankStayType_whenRequested_thenThrowsBusinessException() {
        // GIVEN - WHEN
        BusinessException exception = assertThrows(BusinessException.class,
                () -> roomService.getAvailableRooms(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Simple", " "));

        // THEN
        assertEquals("Debe indicar el tipo de estancia.", exception.getMessage());
    }

    @Test
    void getAvailableRooms_givenMorningReservationWithAccent_whenRequested_thenDetectsOverlap() {
        // GIVEN
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Simple")).thenReturn(List.of(room));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L))).thenReturn(List.of(
                reservation(1L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 20), "Mañana")
        ));

        // WHEN
        List<RoomEntity> rooms = roomService.getAvailableRooms(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 20), "Simple", "Manana");

        // THEN
        assertTrue(rooms.isEmpty());
    }

    @Test
    void getAvailableRooms_givenReservationWithoutDates_whenRequested_thenIgnoresMalformedReservation() {
        // GIVEN
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");
        ReservationEntity malformed = new ReservationEntity();
        malformed.setRoomId(1L);
        malformed.setStayType("Noche");
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Simple")).thenReturn(List.of(room));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L))).thenReturn(List.of(malformed));

        // WHEN
        List<RoomEntity> rooms = roomService.getAvailableRooms(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Simple", "Noche");

        // THEN
        assertEquals(1, rooms.size());
    }

    @Test
    void getAvailableRooms_givenNightReservationNextDay_whenRequestedSameMorning_thenKeepsRoomUnavailableByCleaningBuffer() {
        // GIVEN
        RoomEntity room = buildRoom(1L, "S001", "Simple", "AVAILABLE");
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Simple")).thenReturn(List.of(room));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L))).thenReturn(List.of(
                reservation(1L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 20), "Noche")
        ));

        // WHEN
        List<RoomEntity> rooms = roomService.getAvailableRooms(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 20), "Simple", "Mañana");

        // THEN
        assertTrue(rooms.isEmpty());
    }

    @Test
    void getAvailableRooms_givenMixedRooms_whenRequested_thenReturnsOnlyRoomsWithoutOverlap() {
        // GIVEN
        RoomEntity availableRoom = buildRoom(1L, "S001", "Simple", "AVAILABLE");
        RoomEntity busyRoom = buildRoom(2L, "S002", "Simple", "AVAILABLE");
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Simple")).thenReturn(List.of(availableRoom, busyRoom));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L, 2L))).thenReturn(List.of(
                reservation(2L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Noche")
        ));

        // WHEN
        List<RoomEntity> rooms = roomService.getAvailableRooms(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Simple", "Noche");

        // THEN
        assertEquals(1, rooms.size());
        assertEquals("S001", rooms.get(0).getRoomNumber());
    }

    @Test
    void getAvailableRooms_givenRoomWithoutId_whenRequested_thenSkipsReservationLookupAndKeepsRoomAvailable() {
        // GIVEN
        RoomEntity room = buildRoom(null, "TEMP", "Simple", "AVAILABLE");
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Simple")).thenReturn(List.of(room));

        // WHEN
        List<RoomEntity> rooms = roomService.getAvailableRooms(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "Simple", "Noche");

        // THEN
        assertEquals(1, rooms.size());
        assertEquals("TEMP", rooms.get(0).getRoomNumber());
    }

    private RoomEntity buildRoom(Long id, String number, String type, String status) {
        RoomEntity room = new RoomEntity();
        room.setId(id);
        room.setRoomNumber(number);
        room.setType(type);
        room.setStatus(status);
        return room;
    }

    private ReservationEntity reservation(Long roomId, LocalDate checkIn, LocalDate checkOut, String stayType) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setRoomId(roomId);
        reservation.setCheckInDate(checkIn);
        reservation.setCheckOutDate(checkOut);
        reservation.setStayType(stayType);
        reservation.setCancelled(false);
        return reservation;
    }
}
