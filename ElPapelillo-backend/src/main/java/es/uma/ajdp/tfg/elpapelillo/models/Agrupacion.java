package es.uma.ajdp.tfg.elpapelillo.models;

import es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "agrupaciones")
@Inheritance(strategy = InheritanceType.JOINED) // Estrategia para tablas separadas pero unidas por ID
@Data
@NoArgsConstructor
public class Agrupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String nombreUltimaParticipacion;
    private Integer anio; 

    @Enumerated(EnumType.STRING)
    private CategoriaAgrupacion categoria;

    @Enumerated(EnumType.STRING)
    private EstadoAdministrativo estadoInscripcion;

    // Relación con el Representante (Muchos a Uno)
    @ManyToOne
    @JoinColumn(name = "representante_id")
    private Representante representante;

    // Relación con los Documentos (DNI, autorizaciones...)
    @OneToMany(mappedBy = "agrupacion", cascade = CascadeType.ALL)
    private List<Documento> documentos;

    // Relación con los Participantes
    @OneToMany(mappedBy = "agrupacion", cascade = CascadeType.ALL)
    private List<Participante> participantes;

    // Relación 1 a 1 con la Fianza
    @OneToOne(mappedBy = "agrupacion", cascade = CascadeType.ALL)
    private Fianza fianza;

    // Relación con el Concurso 
    @ManyToOne
    @JoinColumn(name = "concurso_id") // Nombre de la columna en la BD
    private Concurso concurso;
}