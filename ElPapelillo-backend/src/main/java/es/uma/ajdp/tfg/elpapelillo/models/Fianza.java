package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "fianza")
@Data
@NoArgsConstructor
public class Fianza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFianza")
    private Long idFianza;

    private Double importe;
    private LocalDateTime fechaPago;
    private String rutaRecibo; // Para guardar la ruta
    private Boolean pagada;

    @OneToOne
    @JoinColumn(name = "idAgrupacion")
    private Agrupacion agrupacion;
}