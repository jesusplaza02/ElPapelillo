package es.uma.ajdp.tfg.elpapelillo.models;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "participante")
@Data
@NoArgsConstructor
public class Participante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String dni; 
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

}