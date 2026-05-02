package kartingRM.Backend.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String transactionCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    @JsonIgnore
    private ReservationEntity reservation;

    @Column(name = "reservation_id", insertable = false, updatable = false)
    private Long reservationId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 40)
    private String paymentMethod;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    @Column(length = 4)
    private String cardLastFour;

    @PrePersist
    private void prepareDefaults() {
        if (transactionCode == null || transactionCode.isBlank()) {
            transactionCode = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (status == null || status.isBlank()) {
            status = "APPROVED";
        }
        if (paidAt == null) {
            paidAt = LocalDateTime.now();
        }
    }
}
