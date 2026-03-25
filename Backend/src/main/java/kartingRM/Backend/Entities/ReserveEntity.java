package kartingRM.Backend.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;


@Entity
@Table(name = "reserves")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReserveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo_reserva;

    private LocalDate fecha_uso;

    private String hora_inicio;

    private String hora_fin;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private UserEntity cliente;

    private int cantidad_personas;

    private String vueltas_o_tiempo;

    @Column(name = "monto_final")
    private double montoFinal;

    @OneToMany(mappedBy = "reserve", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ReserveDetailsEntity> detalles = new ArrayList<>();

    // para generar el codigo de reserva aleatorio
    @PrePersist
    private void generateCodigoReserva() {
        if (this.codigo_reserva == null || this.codigo_reserva.isEmpty()) {
            this.codigo_reserva = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
    @Override
    public String toString() {
        return "ReserveEntity{" +
                "id=" + id +
                ", fecha_uso=" + fecha_uso +
                ", vueltas_o_tiempo='" + vueltas_o_tiempo + '\'' +
                ", monto_final=" + montoFinal +
                '}';
    }
}