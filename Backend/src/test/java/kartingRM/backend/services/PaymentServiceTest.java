package kartingRM.backend.services;

import kartingRM.Backend.DTOs.PaymentRequest;
import kartingRM.Backend.Entities.PaymentEntity;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.PaymentRepository;
import kartingRM.Backend.Services.PaymentService;
import kartingRM.Backend.Services.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void registerSimulatedPayment_givenValidRequest_thenStoresApprovedPaymentAndConfirmsReservation() {
        ReservationEntity reservation = reservation(10L, 250000.0);
        PaymentRequest request = new PaymentRequest(10L, 250000.0, PaymentService.CREDIT_CARD_SIMULATED,
                "4111 1111 1111 1234", "12/30", "123");

        when(reservationService.getReservationById(10L)).thenReturn(reservation);
        when(paymentRepository.existsByReservationId(10L)).thenReturn(false);
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentEntity payment = paymentService.registerSimulatedPayment(request);

        assertEquals(250000.0, payment.getAmount());
        assertEquals("APPROVED", payment.getStatus());
        assertEquals("1234", payment.getCardLastFour());
        verify(reservationService).confirmReservationPayment(10L, 250000.0);
    }

    @Test
    void registerSimulatedPayment_givenExistingPayment_thenRejectsDuplicate() {
        PaymentRequest request = new PaymentRequest(10L, 100000.0, PaymentService.CREDIT_CARD_SIMULATED,
                "4111111111111111", "12/30", "123");
        when(reservationService.getReservationById(10L)).thenReturn(reservation(10L, 100000.0));
        when(paymentRepository.existsByReservationId(10L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.registerSimulatedPayment(request));

        assertEquals("Solo se puede realizar un pago por reserva.", exception.getMessage());
    }

    @Test
    void registerSimulatedPayment_givenInvalidRequest_thenRejects() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.registerSimulatedPayment(
                        new PaymentRequest(null, 1000.0, PaymentService.CREDIT_CARD_SIMULATED, "1", "12/30", "123")));

        assertEquals("Todo pago debe estar asociado a una reserva existente.", exception.getMessage());
    }

    @Test
    void registerSimulatedPayment_givenInvalidMethod_thenRejects() {
        PaymentRequest request = new PaymentRequest(10L, 1000.0, "CASH", "1", "12/30", "123");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.registerSimulatedPayment(request));

        assertEquals("Solo se acepta tarjeta de credito simulada.", exception.getMessage());
    }

    @Test
    void registerSimulatedPayment_givenMissingCardData_thenRejects() {
        PaymentRequest request = new PaymentRequest(10L, 1000.0, PaymentService.CREDIT_CARD_SIMULATED, "", "12/30", "123");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.registerSimulatedPayment(request));

        assertEquals("Debe ingresar numero de tarjeta, fecha de expiracion y CVV simulados.", exception.getMessage());
    }

    @Test
    void getPaymentByReservation_givenUnknownReservation_thenThrowsNotFound() {
        when(paymentRepository.findByReservationId(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPaymentByReservation(99L));

        assertEquals("Pago no encontrado para la reserva: 99", exception.getMessage());
    }

    @Test
    void getAllPayments_returnsRepositoryData() {
        when(paymentRepository.findAll()).thenReturn(List.of(new PaymentEntity()));

        assertEquals(1, paymentService.getAllPayments().size());
    }

    private ReservationEntity reservation(Long id, double amount) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(id);
        reservation.setFinalAmount(amount);
        reservation.setStatus("PENDING_PAYMENT");
        reservation.setCancelled(false);
        return reservation;
    }
}

