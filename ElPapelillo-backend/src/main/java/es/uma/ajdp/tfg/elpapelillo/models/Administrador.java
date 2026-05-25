package es.uma.ajdp.tfg.elpapelillo.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administrador")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Administrador extends Usuario {

    @Column(nullable = true)
    private String cargo;

    @Column(name = "id_organizacion")
    private Integer id_organizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_organizacion", insertable = false, updatable = false)
    @JsonIgnore
    private Organizacion organizacion;

    @JsonProperty("id_organizacion") 
    public Integer getIdOrganizacionParaJson() {
        return (this.organizacion != null) ? this.organizacion.getIdOrganizacion() : null;
    }

    
    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LogAuditoria> logs = new ArrayList<>();

    

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

    @JsonProperty("nombreOrganizacion") 
    public String getNombreOrganizacionParaJson() {
        return (this.getOrganizacion() != null) ? this.getOrganizacion().getNombre() : "Sin Organización";
    }


}