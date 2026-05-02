package kartingRM.Backend.Services;

import kartingRM.Backend.DTOs.RoomAvailabilityResponse;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Entities.RoomEntity;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.ReservationRepository;
import kartingRM.Backend.Repositories.RoomRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceCoverageTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void getAllRooms_returnsRepositoryRooms() {
        when(roomRepository.findAll()).thenReturn(List.of(room(1L, "S001", "Simple", "AVAILABLE")));

        List<RoomEntity> rooms = roomService.getAllRooms();

        assertEquals(1, rooms.size());
    }

    @Test
    void getRoomById_givenMissingRoom_throwsResourceNotFoundException() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> roomService.getRoomById(999L));

        assertEquals("Habitacion no encontrada con ID: 999", exception.getMessage());
    }

    @Test
    void getRoomAvailabilityOverview_marksRoomAsOccupiedWhenReservationCoversDate() {
        RoomEntity room = room(1L, "S001", "Simple", "AVAILABLE");
        ReservationEntity reservation = reservation(1L, "RSV001",
                LocalDate.of(2026, 4, 26), LocalDate.of(2026, 4, 28));
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L))).thenReturn(List.of(reservation));

        List<RoomAvailabilityResponse> overview = roomService.getRoomAvailabilityOverview(LocalDate.of(2026, 4, 27));

        assertEquals("OCCUPIED", overview.get(0).occupancyStatus());
        assertEquals("RSV001", overview.get(0).reservationCode());
    }

    @Test
    void getRoomAvailabilityOverview_marksRoomAsReservedWhenReservationIsFuture() {
        RoomEntity room = room(1L, "S001", "Simple", "AVAILABLE");
        ReservationEntity reservation = reservation(1L, "RSV002",
                LocalDate.of(2026, 4, 29), LocalDate.of(2026, 4, 30));
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L))).thenReturn(List.of(reservation));

        List<RoomAvailabilityResponse> overview = roomService.getRoomAvailabilityOverview(LocalDate.of(2026, 4, 27));

        assertEquals("RESERVED", overview.get(0).occupancyStatus());
    }

    @Test
    void getRoomAvailabilityOverview_marksRoomAsAvailableWhenNoReservationsExist() {
        RoomEntity room = room(1L, "S001", "Simple", "AVAILABLE");
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(reservationRepository.findByRoomIdInAndCancelledFalse(List.of(1L))).thenReturn(List.of());

        List<RoomAvailabilityResponse> overview = roomService.getRoomAvailabilityOverview(LocalDate.of(2026, 4, 27));

        assertEquals("AVAILABLE", overview.get(0).occupancyStatus());
    }

    @Test
    void ensureDefaultInventory_createsMissingRoomsForEachType() {
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Simple")).thenReturn(List.of(room(1L, "S001", "Simple", "AVAILABLE")));
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Double")).thenReturn(List.of());
        when(roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("Suite")).thenReturn(List.of());
        when(roomRepository.save(any(RoomEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        roomService.ensureDefaultInventory();

        verify(roomRepository, times(109)).save(any(RoomEntity.class));
    }

    private RoomEntity room(Long id, String number, String type, String status) {
        RoomEntity room = new RoomEntity();
        room.setId(id);
        room.setRoomNumber(number);
        room.setType(type);
        room.setStatus(status);
        return room;
    }

    private ReservationEntity reservation(Long roomId, String code, LocalDate checkIn, LocalDate checkOut) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setRoomId(roomId);
        reservation.setReservationCode(code);
        reservation.setCheckInDate(checkIn);
        reservation.setCheckOutDate(checkOut);
        reservation.setStayType("Noche");
        reservation.setCancelled(false);
        return reservation;
    }
}
