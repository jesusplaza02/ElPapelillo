package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "fianzas")
@Data
@NoArgsConstructor
public class Fianza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double importe;
    private LocalDateTime fechaPago;
    private String justificanteUrl; // Para guardar la ruta al PDF del banco

    @OneToOne
    @JoinColumn(name = "agrupacion_id")
    private Agrupacion agrupacion;
}