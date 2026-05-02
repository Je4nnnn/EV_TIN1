package kartingRM.Backend.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import kartingRM.Backend.DTOs.PaymentRequest;
import kartingRM.Backend.Entities.PaymentEntity;
import kartingRM.Backend.Services.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    void getAllPayments_returnsPayments() throws Exception {
        PaymentEntity payment = new PaymentEntity();
        payment.setId(1L);
        payment.setAmount(100000.0);
        when(paymentService.getAllPayments()).thenReturn(List.of(payment));

        mockMvc.perform(get("/api/v1/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(100000.0));
    }

    @Test
    void getPaymentByReservation_returnsPayment() throws Exception {
        PaymentEntity payment = new PaymentEntity();
        payment.setReservationId(10L);
        payment.setAmount(100000.0);
        when(paymentService.getPaymentByReservation(10L)).thenReturn(payment);

        mockMvc.perform(get("/api/v1/payments/reservation/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100000.0));
    }

    @Test
    void simulatePayment_returnsApprovedPayment() throws Exception {
        PaymentEntity payment = new PaymentEntity();
        payment.setStatus("APPROVED");
        when(paymentService.registerSimulatedPayment(any(PaymentRequest.class))).thenReturn(payment);

        PaymentRequest request = new PaymentRequest(10L, 100000.0, PaymentService.CREDIT_CARD_SIMULATED,
                "4111111111111111", "12/30", "123");

        mockMvc.perform(post("/api/v1/payments/simulate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
