package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "logs_sistema")
@Data
@NoArgsConstructor
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "administrador_id")

    private Administrador administrador; // Quién hizo la acción
    private String accion;       // Ejemplo: "CREAR_AGRUPACION", "SUBIR_DNI"
    private String descripcion;  // Detalles de lo que pasó
    private LocalDateTime fecha;

    // PrePersist rellena la fecha automáticamente justo antes de guardarse en la BD
    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }
}