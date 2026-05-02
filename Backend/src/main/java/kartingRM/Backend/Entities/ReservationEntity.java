package kartingRM.Backend.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String reservationCode;

    // Fecha de check-in
    private LocalDate checkInDate;

    // Fecha de check-out (puede ser el mismo día o días después)
    private LocalDate checkOutDate;

    // Tipo de estancia: "Mañana" (turno día), "Noche" (turno noche), "Completo" (día completo)
    private String stayType;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private UserEntity cliente;

    @Column(name = "number_of_guests")
    private Integer numberOfGuests;

    // Tipo de habitación: "Simple", "Double", "Suite"
    private String roomType;

    @Column(name = "final_amount")
    private Double finalAmount;

    @Column(name = "original_amount")
    private Double originalAmount;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "discount_percent")
    private Double discountPercent;

    @Column(name = "discount_breakdown", length = 1000)
    private String discountBreakdown;

    @Column(nullable = false, length = 30)
    private String status = "PENDING_PAYMENT";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "amount_paid")
    private Double amountPaid = 0.0;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "tourist_package_id")
    private Long touristPackageId;

    @Column(name = "tourist_package_name")
    private String touristPackageName;

    @Column(name = "tourist_package_price")
    private Double touristPackagePrice;

    @Column(name = "cancelled", nullable = false)
    private Boolean cancelled = Boolean.FALSE;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ReservationDetailsEntity> details = new ArrayList<>();

    @PrePersist
    private void prepareDefaults() {
        if (this.reservationCode == null || this.reservationCode.isEmpty()) {
            this.reservationCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        if (this.cancelled == null) {
            this.cancelled = Boolean.FALSE;
        }

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.status == null || this.status.isBlank()) {
            this.status = "PENDING_PAYMENT";
        }

        if (this.expiresAt == null && "PENDING_PAYMENT".equalsIgnoreCase(this.status)) {
            this.expiresAt = this.createdAt.plusHours(24);
        }
    }

    @Override
    public String toString() {
        return "ReservationEntity{" +
                "id=" + id +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", stayType='" + stayType + '\'' +
                ", roomType='" + roomType + '\'' +
                ", finalAmount=" + finalAmount +
                '}';
    }
}
