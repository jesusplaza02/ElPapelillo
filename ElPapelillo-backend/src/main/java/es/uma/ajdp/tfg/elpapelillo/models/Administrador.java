package es.uma.ajdp.tfg.elpapelillo.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "administrador")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Administrador extends Usuario {

    @Column(nullable = true)
    private String cargo;

    // 1. Relación con Logs: 1 Admin -> Muchos Logs
    // mappedBy debe coincidir con el nombre del atributo en la clase LogSistema
    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private List<LogAuditoria> logs = new ArrayList<>();

    // 2. Relación con Concursos: Muchos Admin <-> Muchos Concursos
    @ManyToMany
    @JoinTable(
        name = "administrador_concurso", // Nombre de la tabla intermedia
        joinColumns = @JoinColumn(name = "administrador_id"),
        inverseJoinColumns = @JoinColumn(name = "concurso_id")
    )
    private List<Concurso> concursos = new ArrayList<>();

    public Administrador(String email, String password, String nombre, String dni, String telefono, String direccion, String cargo) {
        // Usamos los setters de la clase padre (Usuario)
        this.setEmail(email);
        this.setPassword(password);
        this.setNombre(nombre);
        this.setDNI(dni); 
        this.setTelefono(telefono);
        this.setDireccion(direccion);
        this.setActivo(true); // Aseguramos que nace activo
        this.cargo = cargo;
    }
}