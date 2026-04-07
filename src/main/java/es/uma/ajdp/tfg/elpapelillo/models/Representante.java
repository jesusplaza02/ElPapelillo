package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa al Representante de una Agrupación.
 * Hereda de Usuario con rol REPRESENTANTE.
 */
@Entity
@Table(name = "representantes")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
@NoArgsConstructor
public class Representante extends Usuario {

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Size(max = 15)
    @Column(name = "telefono", length = 15)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agrupacion_id")
    private Agrupacion agrupacion;

    public Representante(String nombre, String apellidos, String dni,
                         String email, String password, String telefono) {
        super(nombre, apellidos, dni, email, password, Rol.REPRESENTANTE);
        this.telefono = telefono;
    }
}
