package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoConcurso;
import es.uma.ajdp.tfg.elpapelillo.models.enums.TipoConcurso;

@Entity
@Table(name = "concurso")
@Data
@NoArgsConstructor
public class Concurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConcurso; 

    private String nombre;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaInicioInscripcion;
    private LocalDate fechaFinInscripcion;
    


    // Usamos @Enumerated para que en la BD se guarde el texto del Enum
    @Enumerated(EnumType.STRING)
    private TipoConcurso tipoConcurso;

    @Enumerated(EnumType.STRING)
    private EstadoConcurso estadoConcurso;

    @ManyToMany(mappedBy = "concursos")
    private List<Administrador> administradores = new ArrayList<>();

    @OneToMany(mappedBy = "concurso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Agrupacion> agrupaciones = new ArrayList<>();

    
    public Concurso(String nombre, LocalDate fechaInicio, LocalDate fechaFin, 
                TipoConcurso tipo, EstadoConcurso estado) {
    this.nombre = nombre;
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
    this.tipoConcurso = tipo;
    this.estadoConcurso = estado;
    }
}