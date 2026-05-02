package kartingRM.Backend.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import java.time.LocalDate;

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
    @Column(unique = true)
    private String email;
    private String phoneNumber;
    private String documentId;
    private String nationality;
    private String role = "CLIENT";
    private Boolean active = Boolean.TRUE;
    private Integer failedLoginAttempts = 0;

    // Fecha de nacimiento del usuario
    private LocalDate dateBirthday;

    private String category_frecuency = "No frecuente";
    private Integer numberVisits = 0;
}
