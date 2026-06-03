package es.uma.ajdp.tfg.elpapelillo.models;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import es.uma.ajdp.tfg.elpapelillo.util.CryptoUtil;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo
    (use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, 
    property = "type", defaultImpl = Usuario.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Administrador.class, name = "administrador"),
    @JsonSubTypes.Type(value = Representante.class, name = "representante")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario")
    private Integer idUsuario;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore 
    private String password; 

    @Column(nullable = false)
    private String DNI;

    @Column(nullable = false)
    private String nombre; 

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String direccion; 

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean activo = true;

    private LocalDate fechaRegistro;

    @Column(nullable = false)
    private String rol;

    // DESCIFRADO AUTOMÁTICO AL LEER DE LA BASE DE DATOS
    @PostLoad
    public void decryptUserData() {
        if (this.getEmail() != null) {
            this.setEmail(CryptoUtil.decrypt(this.getEmail()));
        }
        if (this.getDNI() != null) {
            this.setDNI(CryptoUtil.decrypt(this.getDNI()));
        }
        if (this.getNombre() != null) {
            this.setNombre(CryptoUtil.decrypt(this.getNombre()));
        }
        if (this.getTelefono() != null) {
            this.setTelefono(CryptoUtil.decrypt(this.getTelefono()));
        }
        if (this.getDireccion() != null) {
            this.setDireccion(CryptoUtil.decrypt(this.getDireccion()));
        }
    }

    // CIFRADO AUTOMÁTICO AL CREAR UN NUEVO USUARIO (INSERT)
    @PrePersist
    public void onPrePersist() {
        this.fechaRegistro = LocalDate.now(); 
        encryptData();
    }

    //CIFRADO AUTOMÁTICO AL MODIFICAR UN USUARIO (UPDATE)
    @PreUpdate
    public void onPreUpdate() {
        encryptData();
    }

    private void encryptData() {
        if (this.getEmail() != null) {
            // Pasamos a minúsculas y limpiamos espacios antes de cifrar el email
            this.setEmail(CryptoUtil.encrypt(this.getEmail().trim().toLowerCase()));
        }
        if (this.getDNI() != null) {
            this.setDNI(CryptoUtil.encrypt(this.getDNI().trim().toUpperCase()));
        }
        if (this.getNombre() != null) {
            this.setNombre(CryptoUtil.encrypt(this.getNombre().trim()));
        }
        if (this.getTelefono() != null) {
            this.setTelefono(CryptoUtil.encrypt(this.getTelefono().trim()));
        }
        if (this.getDireccion() != null) {
            this.setDireccion(CryptoUtil.encrypt(this.getDireccion().trim()));
        }
    }

    public Boolean isActivo() {
        return this.activo;
    }
}