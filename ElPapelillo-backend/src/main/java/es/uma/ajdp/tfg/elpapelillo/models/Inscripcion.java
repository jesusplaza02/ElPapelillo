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
import com.fasterxml.jackson.annotation.JsonManagedReference;

import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.models.enums.RolParticipante;

@Entity
@Table(name = "inscripciones")
@Getter 
@Setter 
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
    @JsonIgnoreProperties("inscripciones") 
    private Concurso concurso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_agrupacion")
    @JsonIgnoreProperties("inscripciones") 
    private Agrupacion agrupacion;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_fianza")
    @JsonManagedReference
    private Fianza fianza;

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Documento> documentos = new ArrayList<>();

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participacion> participaciones = new ArrayList<>();


    public void añadirParticipante(Participante participante, RolParticipante rol) {
        Participacion nuevaParticipacion = new Participacion(this, participante, rol);
        this.participaciones.add(nuevaParticipacion);
    }

    public boolean esValida() {
        return !documentos.isEmpty() && estadoInscripcion != EstadoAdministrativo.RECHAZADO;
    }

    public List<Participante> getParticipantes() {
        List<Participante> listaParticipantes = new ArrayList<>();
        if (this.participaciones != null) {
            for (Participacion p : this.participaciones) {
                if (p.getParticipante() != null) {
                    listaParticipantes.add(p.getParticipante());
                }
            }
        }
        return listaParticipantes;
    }


    //public void setParticipantes(List<Participante> participantes) {
    //}
}