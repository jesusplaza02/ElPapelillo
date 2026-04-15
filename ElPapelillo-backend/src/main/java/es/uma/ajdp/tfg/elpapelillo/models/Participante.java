package es.uma.ajdp.tfg.elpapelillo.models;

import es.uma.ajdp.tfg.elpapelillo.models.enums.RolParticipante;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "participante")
@Data
@NoArgsConstructor
public class Participante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String dni; // Este lo cifraremos después

    @ManyToOne
    @JoinColumn(name = "agrupacion_id")
    private Agrupacion agrupacion;

    @Enumerated(EnumType.STRING)
    private RolParticipante rol;
}