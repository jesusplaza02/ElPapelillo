package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa a un Administrador del sistema.
 * Hereda de Usuario con rol ADMINISTRADOR.
 */
@Entity
@Table(name = "administradores")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
@NoArgsConstructor
public class Administrador extends Usuario {

    @Column(name = "nivel_acceso", nullable = false)
    private int nivelAcceso = 1;

    public Administrador(String nombre, String apellidos, String dni,
                         String email, String password, int nivelAcceso) {
        super(nombre, apellidos, dni, email, password, Rol.ADMINISTRADOR);
        this.nivelAcceso = nivelAcceso;
    }
}
