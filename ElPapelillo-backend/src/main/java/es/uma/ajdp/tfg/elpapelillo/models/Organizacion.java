package es.uma.ajdp.tfg.elpapelillo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "organizacion")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Organizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOrganizacion")
    @JsonProperty("idOrganizacion")
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

    @OneToMany(mappedBy = "organizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Concurso> concursos = new ArrayList<>();

    @OneToMany(mappedBy = "organizacion", cascade = CascadeType.ALL)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Administrador> administradores = new ArrayList<>();

    public Organizacion(String nombre, String email, String telefono, String ubicacion) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.ubicacion = ubicacion;
        this.activo = true; // Por defecto, la organización nace activa

    }
}