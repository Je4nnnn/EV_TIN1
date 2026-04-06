package kartingRM.Backend.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "reservation_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReservationDetailsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String guestName;

    private Double finalAmount;

    private Double discount;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    @JsonBackReference
    private ReservationEntity reservation;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Override
    public String toString() {
        return "ReservationDetailsEntity{" +
                "id=" + id +
                ", guestName='" + guestName + '\'' +
                ", finalAmount=" + finalAmount +
                ", discount=" + discount +
                '}';
    }
}
