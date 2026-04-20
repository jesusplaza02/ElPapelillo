package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "representante")
@PrimaryKeyJoinColumn(name = "idUsuario")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Representante extends Usuario {

    
    @OneToMany(mappedBy = "representante", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JsonManagedReference
    @JsonIgnore
    private List<Agrupacion> agrupaciones = new ArrayList<>();

    @Column(name = "contacto_emergencia") 
    private String contacto_emergencia;

    public Representante(String email, String password, String nombre, String dni, String telefono, String direccion, String contactoEmergencia) {
        this.setEmail(email);
        this.setPassword(password);
        this.setNombre(nombre);
        this.setDNI(dni);
        this.setTelefono(telefono);
        this.setDireccion(direccion);
        this.setActivo(true);
        this.setContacto_emergencia(contactoEmergencia);
    }
}