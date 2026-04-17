package es.uma.ajdp.tfg.elpapelillo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "documento")
@Data
@ToString(exclude = "agrupacion")
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDocumento;

    private String nombre;
    private String tipo; // Ejemplo: "DNI", "AUTORIZACION_MENOR"
    private String urlArchivo;
    private String comentarioRevision;


    @Enumerated(EnumType.STRING)
    private EstadoAdministrativo estado;

    @ManyToOne
    @JoinColumn(name = "idAgrupacion")
    @JsonIgnore
    private Agrupacion agrupacion;

    // ¡Asegúrate de que este método existe!
    public void setAgrupacion(Agrupacion agrupacion) {
        this.agrupacion = agrupacion;
    }
    public Agrupacion getAgrupacion() {
        return agrupacion;
    }
}