package kartingRM.Backend.Services;

import kartingRM.Backend.DTOs.PaymentRequest;
import kartingRM.Backend.Entities.PaymentEntity;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentService {

    public static final String CREDIT_CARD_SIMULATED = "CREDIT_CARD_SIMULATED";

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationService reservationService;

    @Transactional(readOnly = true)
    public List<PaymentEntity> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PaymentEntity getPaymentByReservation(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado para la reserva: " + reservationId));
    }

    @Transactional
    public PaymentEntity registerSimulatedPayment(PaymentRequest request) {
        validatePaymentRequest(request);

        ReservationEntity reservation = reservationService.getReservationById(request.reservationId());
        if (paymentRepository.existsByReservationId(reservation.getId())) {
            throw new BusinessException("Solo se puede realizar un pago por reserva.");
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setReservation(reservation);
        payment.setAmount(request.amount());
        payment.setPaymentMethod(CREDIT_CARD_SIMULATED);
        payment.setStatus("APPROVED");
        payment.setCardLastFour(resolveLastFour(request.cardNumber()));

        PaymentEntity savedPayment = paymentRepository.save(payment);
        reservationService.confirmReservationPayment(reservation.getId(), request.amount());
        return savedPayment;
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request == null || request.reservationId() == null) {
            throw new BusinessException("Todo pago debe estar asociado a una reserva existente.");
        }

        if (request.amount() == null || request.amount() <= 0) {
            throw new BusinessException("El monto del pago debe ser mayor que cero.");
        }

        if (request.paymentMethod() == null || !CREDIT_CARD_SIMULATED.equalsIgnoreCase(request.paymentMethod().trim())) {
            throw new BusinessException("Solo se acepta tarjeta de credito simulada.");
        }

        if (isBlank(request.cardNumber()) || isBlank(request.expirationDate()) || isBlank(request.cvv())) {
            throw new BusinessException("Debe ingresar numero de tarjeta, fecha de expiracion y CVV simulados.");
        }
    }

    private String resolveLastFour(String cardNumber) {
        String digits = cardNumber.replaceAll("\\D", "");
        if (digits.length() <= 4) {
            return digits;
        }
        return digits.substring(digits.length() - 4);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

