package kartingRM.Backend.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Table(name = "karts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;
    private String code;
    private String model;
    private String status;
    
}
