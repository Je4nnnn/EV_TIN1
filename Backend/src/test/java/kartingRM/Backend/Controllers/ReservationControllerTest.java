package kartingRM.Backend.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Exceptions.GlobalExceptionHandler;
import kartingRM.Backend.Services.ReservationService;
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
import java.util.Map;

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
class ReservationControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reservationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllReservations_returnsReservations() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(List.of(reservation(1L, "RSV001")));

        mockMvc.perform(get("/api/v1/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationCode").value("RSV001"));
    }

    @Test
    void getReservationById_returnsReservation() throws Exception {
        when(reservationService.getReservationById(1L)).thenReturn(reservation(1L, "RSV001"));

        mockMvc.perform(get("/api/v1/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addReservation_returnsSavedReservation() throws Exception {
        when(reservationService.saveReservation(any(ReservationEntity.class))).thenReturn(reservation(2L, "RSV002"));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation(null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationCode").value("RSV002"));
    }

    @Test
    void confirmarReserva_returnsSavedReservation() throws Exception {
        when(reservationService.saveReservation(any(ReservationEntity.class))).thenReturn(reservation(3L, "RSV003"));

        mockMvc.perform(post("/api/v1/reservations/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation(null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationCode").value("RSV003"));
    }

    @Test
    void updateReservation_returnsUpdatedReservation() throws Exception {
        when(reservationService.updateReservation(eq(4L), any(ReservationEntity.class))).thenReturn(reservation(4L, "RSV004"));

        mockMvc.perform(put("/api/v1/reservations/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation(null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void deleteReservation_returnsConfirmationMessage() throws Exception {
        doNothing().when(reservationService).deleteReservation(5L);

        mockMvc.perform(delete("/api/v1/reservations/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reserva eliminada correctamente."));
    }

    @Test
    void getReportePorTipoHabitacion_returnsReport() throws Exception {
        when(reservationService.getReporteIngresosPorVueltasOTiempo(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        )).thenReturn(Map.of("Simple", Map.of("Noche", 120000.0)));

        mockMvc.perform(get("/api/v1/reservations/reports/room-type")
                        .param("fechaInicio", "2026-04-01")
                        .param("fechaFin", "2026-04-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Simple.Noche").value(120000.0));
    }

    @Test
    void getReportePorCantidadPersonas_returnsReport() throws Exception {
        when(reservationService.getReporteIngresosPorCantidadDePersonas(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        )).thenReturn(Map.of("2 personas", Map.of("Completo", 90000.0)));

        mockMvc.perform(get("/api/v1/reservations/reports/guest-count")
                        .param("fechaInicio", "2026-04-01")
                        .param("fechaFin", "2026-04-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['2 personas'].Completo").value(90000.0));
    }

    private ReservationEntity reservation(Long id, String code) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(id);
        reservation.setReservationCode(code);
        reservation.setCheckInDate(LocalDate.of(2026, 4, 27));
        reservation.setCheckOutDate(LocalDate.of(2026, 4, 28));
        reservation.setStayType("Noche");
        reservation.setRoomType("Simple");
        reservation.setNumberOfGuests(2);
        reservation.setFinalAmount(75000.0);
        reservation.setCancelled(false);
        return reservation;
    }
}
