package es.uma.ajdp.tfg.elpapelillo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "documento")
@Data
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDocumento;

    private String nombre;
    private String tipo; // Ejemplo: "DNI", "AUTORIZACION_MENOR"
    private String urlArchivo;
    private String comentarioRevision;


    @Enumerated(EnumType.STRING)
    private EstadoAdministrativo estado;

    @ManyToOne
    @JoinColumn(name = "id_inscripcion")
    @JsonIgnore
    private Inscripcion inscripcion;

}