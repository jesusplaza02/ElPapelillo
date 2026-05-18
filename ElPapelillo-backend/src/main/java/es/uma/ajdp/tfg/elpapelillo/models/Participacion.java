package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.uma.ajdp.tfg.elpapelillo.models.enums.RolParticipante;

@Entity
@Table(name = "inscripcion_participantes")
@Getter
@Setter
@NoArgsConstructor
public class Participacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idParticipacion;

    @ManyToOne
    @JoinColumn(name = "id_inscripcion")
    @JsonIgnore
    private Inscripcion inscripcion;

    @ManyToOne
    @JoinColumn(name = "id_participante")
    private Participante participante;

    @Enumerated(EnumType.STRING)
    private RolParticipante rol;

    // Constructor necesario para el método añadirParticipante de la clase Inscripcion
    public Participacion(Inscripcion inscripcion, Participante participante, RolParticipante rol) {
        this.inscripcion = inscripcion;
        this.participante = participante;
        this.rol = rol;
    }
}