package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un Concurso de Agrupaciones Carnavalescas.
 */
@Entity
@Table(name = "concursos")
@Getter
@Setter
@NoArgsConstructor
public class Concurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del concurso no puede estar vacío")
    @Size(max = 200)
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @NotNull(message = "La fecha de inicio no puede ser nula")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin no puede ser nula")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @NotBlank(message = "El lugar no puede estar vacío")
    @Size(max = 200)
    @Column(name = "lugar", length = 200)
    private String lugar;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoConcurso estado = EstadoConcurso.ABIERTO;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @OneToMany(mappedBy = "concurso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Agrupacion> agrupaciones = new ArrayList<>();

    public Concurso(String nombre, LocalDate fechaInicio, LocalDate fechaFin, String lugar) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.lugar = lugar;
    }

    /**
     * Estados posibles de un concurso.
     */
    public enum EstadoConcurso {
        ABIERTO,
        EN_CURSO,
        CERRADO,
        CANCELADO
    }
}
