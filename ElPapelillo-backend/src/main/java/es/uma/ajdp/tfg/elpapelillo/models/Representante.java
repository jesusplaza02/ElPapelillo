package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("REPRESENTANTE") 
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Representante extends Usuario {

    
    @OneToMany(mappedBy = "representante", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<Agrupacion> agrupaciones = new ArrayList<>();

    public Representante(String email, String password, String nombre, String dni, String telefono, String direccion) {
        this.setEmail(email);
        this.setPassword(password);
        this.setNombre(nombre);
        this.setDNI(dni);
        this.setTelefono(telefono);
        this.setDireccion(direccion);
        this.setActivo(true);
    }
}