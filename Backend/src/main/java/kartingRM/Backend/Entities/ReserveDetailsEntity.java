package kartingRM.Backend.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "reserve_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReserveDetailsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String memberName;

    private LocalDate dateBirthday;

    private double montoFinal;

    private double discount;

    @ManyToOne
    @JoinColumn(name = "reserve_id")
    @JsonBackReference
    private ReserveEntity reserve;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Override
    public String toString() {
        return "ReserveDetailsEntity{" +
                "id=" + id +
                ", memberName='" + memberName + '\'' +
                ", montoFinal=" + montoFinal +
                ", discount=" + discount +
                '}';
    }
}

