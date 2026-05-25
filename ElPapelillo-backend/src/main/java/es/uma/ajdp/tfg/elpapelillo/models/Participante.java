package es.uma.ajdp.tfg.elpapelillo.models;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import es.uma.ajdp.tfg.elpapelillo.util.CryptoUtil;

@Entity
@Table(name = "participante")
@Data
@NoArgsConstructor
public class Participante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String dni; 
    
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @PostLoad
    public void decryptDni() {
        this.dni = CryptoUtil.decrypt(this.dni);
    }

    @PrePersist
    public void encryptDniOnPersist() {
        this.dni = CryptoUtil.encrypt(this.dni);
    }

    @PreUpdate
    public void encryptDniOnUpdate() {
        this.dni = CryptoUtil.encrypt(this.dni);
    }
}