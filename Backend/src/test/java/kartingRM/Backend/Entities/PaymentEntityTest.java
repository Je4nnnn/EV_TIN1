package kartingRM.Backend.Entities;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentEntityTest {

    @Test
    void prepareDefaults_givenBlankFields_thenAssignsTransactionStatusAndDate() throws Exception {
        PaymentEntity payment = new PaymentEntity();
        payment.setTransactionCode(" ");

        invokePrepareDefaults(payment);

        assertNotNull(payment.getTransactionCode());
        assertTrue(payment.getTransactionCode().startsWith("PAY-"));
        assertEquals("APPROVED", payment.getStatus());
        assertNotNull(payment.getPaidAt());
    }

    @Test
    void prepareDefaults_givenExplicitValues_thenKeepsThem() throws Exception {
        LocalDateTime paidAt = LocalDateTime.of(2026, 5, 2, 10, 0);
        PaymentEntity payment = new PaymentEntity();
        payment.setTransactionCode("PAY-12345678");
        payment.setStatus("APPROVED");
        payment.setPaidAt(paidAt);

        invokePrepareDefaults(payment);

        assertEquals("PAY-12345678", payment.getTransactionCode());
        assertEquals("APPROVED", payment.getStatus());
        assertEquals(paidAt, payment.getPaidAt());
    }

    private void invokePrepareDefaults(PaymentEntity payment) throws Exception {
        Method method = PaymentEntity.class.getDeclaredMethod("prepareDefaults");
        method.setAccessible(true);
        method.invoke(payment);
    }
}
