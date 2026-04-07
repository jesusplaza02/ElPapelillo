package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una Agrupación inscrita en un Concurso.
 */
@Entity
@Table(name = "agrupaciones")
@Getter
@Setter
@NoArgsConstructor
public class Agrupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la agrupación no puede estar vacío")
    @Size(max = 200)
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", nullable = false)
    private Modalidad modalidad;

    @Column(name = "numero_componentes")
    private Integer numeroComponentes;

    @Size(max = 100)
    @Column(name = "municipio", length = 100)
    private String municipio;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concurso_id", nullable = false)
    private Concurso concurso;

    @OneToMany(mappedBy = "agrupacion", cascade = CascadeType.ALL)
    private List<Representante> representantes = new ArrayList<>();

    public Agrupacion(String nombre, Modalidad modalidad, String municipio, Concurso concurso) {
        this.nombre = nombre;
        this.modalidad = modalidad;
        this.municipio = municipio;
        this.concurso = concurso;
    }

    /**
     * Modalidades de agrupaciones carnavalescas.
     */
    public enum Modalidad {
        COMPARSA,
        CHIRIGOTA,
        CORO,
        CUARTETO,
        ROMANCERO
    }
}
