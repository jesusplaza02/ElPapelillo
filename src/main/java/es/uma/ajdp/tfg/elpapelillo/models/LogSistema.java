package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que registra las acciones realizadas en el sistema.
 */
@Entity
@Table(name = "logs_sistema")
@Getter
@Setter
@NoArgsConstructor
public class LogSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La acción no puede estar vacía")
    @Column(name = "accion", nullable = false, length = 255)
    private String accion;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false)
    private NivelLog nivel = NivelLog.INFO;

    @PrePersist
    public void prePersist() {
        if (this.fechaHora == null) {
            this.fechaHora = LocalDateTime.now();
        }
    }

    public LogSistema(String accion, String descripcion, Usuario usuario, NivelLog nivel) {
        this.accion = accion;
        this.descripcion = descripcion;
        this.usuario = usuario;
        this.nivel = nivel;
        this.fechaHora = LocalDateTime.now();
    }

    /**
     * Niveles de severidad del log.
     */
    public enum NivelLog {
        INFO,
        WARNING,
        ERROR,
        CRITICO
    }
}
