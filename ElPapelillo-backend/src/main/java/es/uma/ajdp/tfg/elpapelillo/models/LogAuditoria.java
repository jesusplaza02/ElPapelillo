package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "logauditoria")
@Data
@NoArgsConstructor
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "administrador_id")
    @JsonIgnore
    private Administrador administrador; 

    @JsonProperty("idUsuario")
    public Integer getIdUsuario() {
        return (administrador != null) ? administrador.getIdUsuario() : null;
    }

    private String accion;       // Ejemplo: "CREAR_AGRUPACION", "SUBIR_DNI"
    private String descripcion;  // Detalles de lo que pasó
    private LocalDateTime fecha;

    public LogAuditoria(Administrador admin, String accion, String descripcion) {
        this.administrador = admin;
        this.accion = accion;
        this.descripcion = descripcion;
    }
    // PrePersist rellena la fecha automáticamente justo antes de guardarse en la BD
    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }
}