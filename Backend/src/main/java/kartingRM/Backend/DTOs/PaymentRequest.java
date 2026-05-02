package kartingRM.Backend.DTOs;

public record PaymentRequest(
        Long reservationId,
        Double amount,
        String paymentMethod,
        String cardNumber,
        String expirationDate,
        String cvv
) {
}

