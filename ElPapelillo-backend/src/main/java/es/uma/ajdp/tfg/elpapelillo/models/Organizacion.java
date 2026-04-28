package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "organizacion")
@Data
@NoArgsConstructor
public class Organizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOrganizacion")
    private Integer idOrganizacion;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String email; 

    @Column(nullable = false)
    private String telefono;   

    @Column(nullable = false)
    private String ubicacion; 

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean activo=true;


    // Relación 1:N con Concurso
    // Una organización puede tener muchos concursos (ej. Concurso de drags, Concurso de canto, etc.)
    @OneToMany(mappedBy = "organizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Concurso> concursos = new ArrayList<>();

    // Relación 1:N con Administrador
    // Los administradores pertenecen a una organización específica
    @OneToMany(mappedBy = "organizacion", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Administrador> administradores = new ArrayList<>();

    public Organizacion(String nombre, String email, String telefono, String ubicacion) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.ubicacion = ubicacion;
        this.activo = true; // Por defecto, la organización nace activa

    }
}