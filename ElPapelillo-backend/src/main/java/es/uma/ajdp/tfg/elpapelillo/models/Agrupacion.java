package es.uma.ajdp.tfg.elpapelillo.models;

import es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.models.enums.TipoConcurso;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "agrupacion")
@Inheritance(strategy = InheritanceType.JOINED)

@Data
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

    @Enumerated(EnumType.STRING)
    @Column(name = "estadoInscripcion") // Coincide con 'estadoInscripcion' en la BD
    private EstadoAdministrativo estadoInscripcion;

    @ManyToOne
    @JoinColumn(name = "idRepresentante")
    @JsonBackReference
    private Representante representante;

    @ManyToOne
    @JoinColumn(name = "idConcurso")
    @JsonIgnoreProperties("agrupaciones")
    private Concurso concurso;

    @OneToMany(mappedBy = "agrupacion", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("agrupacion")
    private List<Documento> documentos;

    @OneToMany(mappedBy = "agrupacion", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("agrupacion")
    private List<Participante> participantes;

    @OneToOne(mappedBy = "agrupacion", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("agrupacion")
    private Fianza fianza;
}