package kartingRM.Backend.controllers;

import kartingRM.Backend.Controllers.RoomController;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roomController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllRooms_returnsRoomList() throws Exception {
        when(roomService.getAllRooms()).thenReturn(List.of(new RoomEntity(1L, "S001", "Simple", "AVAILABLE")));

        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomNumber").value("S001"))
                .andExpect(jsonPath("$[0].type").value("Simple"));
    }

    @Test
    void getRoomOverview_returnsAvailabilityPayload() throws Exception {
        when(roomService.getRoomAvailabilityOverview(any(LocalDate.class))).thenReturn(List.of(
                new RoomAvailabilityResponse(1L, "S001", "Simple", "AVAILABLE", "AVAILABLE", null, null, null)
        ));

        mockMvc.perform(get("/api/v1/rooms/overview").param("referenceDate", "2026-05-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomNumber").value("S001"))
                .andExpect(jsonPath("$[0].occupancyStatus").value("AVAILABLE"));
    }

    @Test
    void addRoom_persistsRoom() throws Exception {
        when(roomService.saveRoom(any(RoomEntity.class))).thenReturn(new RoomEntity(1L, "S010", "Suite", "AVAILABLE"));

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomNumber": "S010",
                                  "type": "Suite",
                                  "status": "AVAILABLE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomNumber").value("S010"));
    }
}
