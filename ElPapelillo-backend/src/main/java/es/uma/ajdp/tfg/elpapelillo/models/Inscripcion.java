package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.models.enums.RolParticipante;

@Entity
@Table(name = "inscripciones")
@Getter // Mejor que @Data
@Setter // Mejor que @Data
@NoArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer idInscripcion;

    @Column(nullable = false)
    private LocalDateTime fechaInscripcion;

    @Enumerated(EnumType.STRING)
    private EstadoAdministrativo estadoInscripcion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_concurso")
    @JsonIgnoreProperties("inscripciones") // Evita que el concurso vuelva a cargar esta lista
    private Concurso concurso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_agrupacion")
    @JsonIgnoreProperties("inscripciones") // Evita que la agrupación vuelva a cargar esta lista
    private Agrupacion agrupacion;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_fianza")
    @JsonIgnore
    private Fianza fianza;

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Documento> documentos = new ArrayList<>();

    // Apunta a la clase de asociación Participacion
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Participacion> participaciones = new ArrayList<>();

    // --- MÉTODOS DE LÓGICA DE NEGOCIO ---

    public void añadirParticipante(Participante participante, RolParticipante rol) {
        // Creamos la instancia de la clase de asociación que une ambos
        Participacion nuevaParticipacion = new Participacion(this, participante, rol);
        this.participaciones.add(nuevaParticipacion);
    }

    public boolean esValida() {
        return !documentos.isEmpty() && estadoInscripcion != EstadoAdministrativo.RECHAZADO;
    }
}