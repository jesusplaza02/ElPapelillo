package es.uma.ajdp.tfg.elpapelillo.models;

import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "documento")
@Data
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String tipo; // Ejemplo: "DNI", "AUTORIZACION_MENOR"
    private String urlArchivo;

    @Enumerated(EnumType.STRING)
    private EstadoAdministrativo estado;

    @ManyToOne
    @JoinColumn(name = "agrupacion_id")
    private Agrupacion agrupacion;
}