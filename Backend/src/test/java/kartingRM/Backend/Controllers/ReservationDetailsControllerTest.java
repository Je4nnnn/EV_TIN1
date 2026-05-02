package kartingRM.Backend.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import kartingRM.Backend.Entities.ReservationDetailsEntity;
import kartingRM.Backend.Exceptions.GlobalExceptionHandler;
import kartingRM.Backend.Services.ReservationDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReservationDetailsControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private ReservationDetailsService reservationDetailsService;

    @InjectMocks
    private ReservationDetailsController reservationDetailsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reservationDetailsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllReservationDetails_returnsAllDetails() throws Exception {
        when(reservationDetailsService.getAllReservationDetails()).thenReturn(List.of(detail(1L, "Maria")));

        mockMvc.perform(get("/api/v1/reservation-details/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].guestName").value("Maria"));
    }

    @Test
    void getReservationDetailById_returnsDetail() throws Exception {
        when(reservationDetailsService.getReservationDetailById(1L)).thenReturn(detail(1L, "Pedro"));

        mockMvc.perform(get("/api/v1/reservation-details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestName").value("Pedro"));
    }

    @Test
    void addReservationDetail_returnsSavedDetail() throws Exception {
        when(reservationDetailsService.saveReservationDetail(any(ReservationDetailsEntity.class))).thenReturn(detail(2L, "Sofia"));

        mockMvc.perform(post("/api/v1/reservation-details/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(detail(null, "Sofia"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void updateReservationDetail_returnsUpdatedDetail() throws Exception {
        when(reservationDetailsService.updateReservationDetail(eq(3L), any(ReservationDetailsEntity.class))).thenReturn(detail(3L, "Diego"));

        mockMvc.perform(put("/api/v1/reservation-details/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(detail(null, "Diego"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestName").value("Diego"));
    }

    @Test
    void deleteReservationDetail_returnsOk() throws Exception {
        doNothing().when(reservationDetailsService).deleteReservationDetail(4L);

        mockMvc.perform(delete("/api/v1/reservation-details/4"))
                .andExpect(status().isOk());
    }

    private ReservationDetailsEntity detail(Long id, String guestName) {
        ReservationDetailsEntity detail = new ReservationDetailsEntity();
        detail.setId(id);
        detail.setGuestName(guestName);
        detail.setDiscount(0.0);
        detail.setFinalAmount(42000.0);
        detail.setUserId(10L);
        return detail;
    }
}
