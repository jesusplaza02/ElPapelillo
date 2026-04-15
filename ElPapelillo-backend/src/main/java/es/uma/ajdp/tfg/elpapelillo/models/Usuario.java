package es.uma.ajdp.tfg.elpapelillo.models;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "usuario")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)


public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario")
    private Integer idUsuario;

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

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean activo = true;

    //@Column( nullable = false)
    private LocalDate fechaRegistro;

    @Column(nullable = false)
    private String rol;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDate.now(); // Se guarda la fecha del servidor al crear el registro
    }

    public String getRol() {
        return this.rol;
    }
}