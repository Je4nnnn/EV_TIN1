package kartingRM.Backend.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String rut; // Tiene que ser unico ya que del rut se va a sacar el id del cliente.

    private String name;
    private String email;
    private String phoneNumber;
    private String category_frecuency = "No frecuente";
    private int numberVisits = 0;
}