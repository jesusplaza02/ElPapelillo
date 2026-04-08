package es.uma.ajdp.tfg.elpapelillo.models;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Todos a una sola tabla
@DiscriminatorColumn(
    name = "rol",                           
    discriminatorType = DiscriminatorType.STRING 
)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String DNI;

    //Datos que deberán ser cifrados
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private String telefono;
    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDate.now(); // Se guarda la fecha del servidor al crear el registro
    }

    public String getRol() {
    return this.getClass().getAnnotation(DiscriminatorValue.class).value();
    }
}