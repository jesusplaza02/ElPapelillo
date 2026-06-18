package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoConcurso;
import es.uma.ajdp.tfg.elpapelillo.models.enums.TipoConcurso;

@Entity
@Table(name = "concurso")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Concurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idConcurso; 

    private String nombre;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaInicioInscripcion;
    private LocalDate fechaFinInscripcion;
    
    @Enumerated(EnumType.STRING)
    private TipoConcurso tipoConcurso;

    @Enumerated(EnumType.STRING)
    private EstadoConcurso estadoConcurso;

    
    @Column(name = "id_organizacion")
    private Integer id_organizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_organizacion", nullable = false, insertable = false, updatable = false)
    @JsonBackReference
    private Organizacion organizacion;

    @OneToMany(mappedBy = "concurso")
    @JsonIgnore 
    private List<Inscripcion> inscripciones;
    
    public Concurso(String nombre, LocalDate fechaInicio, LocalDate fechaFin, 
                TipoConcurso tipo, EstadoConcurso estado) {
    this.nombre = nombre;
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
    this.tipoConcurso = tipo;
    this.estadoConcurso = estado;
    }

    @JsonProperty("nombreOrganizacion") 
    public String getNombreOrganizacionParaJson() {
        return (this.organizacion != null) ? this.organizacion.getNombre() : "Sin Organización";
    }

    public Integer getId_organizacion() { return id_organizacion; }
    public void setId_organizacion(Integer id_organizacion) { this.id_organizacion = id_organizacion; }
}