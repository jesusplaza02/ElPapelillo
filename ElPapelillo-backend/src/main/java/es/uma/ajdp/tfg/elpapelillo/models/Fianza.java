package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "fianza")
@Data
@NoArgsConstructor
public class Fianza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFianza")
    private Integer idFianza;

    private Double importe;
    private LocalDateTime fechaPago;
    private String rutaRecibo; // Para guardar la ruta
    private Boolean pagada;

    @OneToOne(mappedBy = "fianza")
    @JsonIgnore
    private Inscripcion inscripcion;
}