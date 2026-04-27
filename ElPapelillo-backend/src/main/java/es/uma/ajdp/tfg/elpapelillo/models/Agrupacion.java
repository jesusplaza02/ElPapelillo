package es.uma.ajdp.tfg.elpapelillo.models;

import es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.enums.TipoConcurso;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Entity
@Table(name = "agrupacion")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Agrupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAgrupacion")
    private Integer idAgrupacion;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "nombreUltimaParticipacion")
    private String nombreUltimaParticipacion;

    @Column(name = "anio") 
    private Integer anio;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria")
    private CategoriaAgrupacion categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoConcurso")
    private TipoConcurso tipoConcurso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_representante")
    @JsonIgnoreProperties({"agrupaciones", "password", "roles"}) // Corta la vuelta al representante
    private Representante representante;

    @OneToMany(mappedBy = "agrupacion")
    @JsonIgnore // NUNCA serializar inscripciones desde aquí
    private List<Inscripcion> inscripciones;
}