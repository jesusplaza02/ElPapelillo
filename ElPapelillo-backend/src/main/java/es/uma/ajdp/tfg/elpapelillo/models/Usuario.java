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
//CONFIGURACIÓN DE JACKSON PARA EVITAR RECURSIVIDAD Y ERRORES DE HERENCIA:
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
    @JsonIgnore // SEGURIDAD: Impide que la contraseña se envíe al Frontend
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
    private Boolean activo=true;

    private LocalDate fechaRegistro;

    @Column(nullable = false)
    private String rol;


    @PostLoad
    public void decryptDni() {
        if (this.getDNI() != null) {
            this.setDNI(CryptoUtil.decrypt(this.getDNI()));
        }
    }

    @PrePersist
    public void onPrePersist() {
        this.fechaRegistro = LocalDate.now(); 
        
        if (this.getDNI() != null) {
            String dniLimpio = this.getDNI().trim().toUpperCase();
            this.setDNI(CryptoUtil.encrypt(dniLimpio));
        }
    }

    @PreUpdate
    public void encryptDniOnUpdate() {
        if (this.getDNI() != null) {
            String dniLimpio = this.getDNI().trim().toUpperCase();
            this.setDNI(CryptoUtil.encrypt(dniLimpio));
        }
    }

    public Boolean isActivo() {
        return this.activo;
    }
}