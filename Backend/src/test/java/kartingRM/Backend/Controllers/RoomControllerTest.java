package kartingRM.Backend.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import kartingRM.Backend.DTOs.RoomAvailabilityResponse;
import kartingRM.Backend.Entities.RoomEntity;
import kartingRM.Backend.Exceptions.GlobalExceptionHandler;
import kartingRM.Backend.Services.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roomController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllRooms_returnsRoomList() throws Exception {
        when(roomService.getAllRooms()).thenReturn(List.of(room(1L, "S001", "Simple", "AVAILABLE")));

        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomNumber").value("S001"));
    }

    @Test
    void getRoomById_returnsRequestedRoom() throws Exception {
        when(roomService.getRoomById(1L)).thenReturn(room(1L, "D010", "Double", "AVAILABLE"));

        mockMvc.perform(get("/api/v1/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Double"));
    }

    @Test
    void getRoomOverview_returnsAvailabilityOverview() throws Exception {
        RoomAvailabilityResponse overview = new RoomAvailabilityResponse(
                1L,
                "S001",
                "Simple",
                "AVAILABLE",
                "AVAILABLE",
                null,
                null,
                null
        );
        when(roomService.getRoomAvailabilityOverview(LocalDate.of(2026, 4, 27))).thenReturn(List.of(overview));

        mockMvc.perform(get("/api/v1/rooms/overview").param("referenceDate", "2026-04-27"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].occupancyStatus").value("AVAILABLE"));
    }

    @Test
    void getAvailableRooms_returnsMatchingRooms() throws Exception {
        when(roomService.getAvailableRooms(
                LocalDate.of(2026, 4, 27),
                LocalDate.of(2026, 4, 28),
                "Simple",
                "Noche"
        )).thenReturn(List.of(room(2L, "S002", "Simple", "AVAILABLE")));

        mockMvc.perform(get("/api/v1/rooms/available")
                        .param("checkInDate", "2026-04-27")
                        .param("checkOutDate", "2026-04-28")
                        .param("roomType", "Simple")
                        .param("stayType", "Noche"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void addRoom_persistsRoom() throws Exception {
        RoomEntity payload = room(null, "SU001", "Suite", "AVAILABLE");
        RoomEntity saved = room(3L, "SU001", "Suite", "AVAILABLE");
        when(roomService.saveRoom(any(RoomEntity.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.roomNumber").value("SU001"));
    }

    private RoomEntity room(Long id, String number, String type, String status) {
        RoomEntity room = new RoomEntity();
        room.setId(id);
        room.setRoomNumber(number);
        room.setType(type);
        room.setStatus(status);
        return room;
    }
}
