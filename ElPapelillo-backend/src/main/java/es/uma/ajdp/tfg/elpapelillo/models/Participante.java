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

    @Column(nullable = false)
    private String nombre; 

    @Column(nullable = false)
    private String dni;   

    // 🔒 Esta es la columna REAL en MySQL que almacenará el String cifrado de la fecha
    @Column(name = "fecha_nacimiento", nullable = false)
    private String fechaNacimientoCifrada;

    // 💡 Este campo NO se mapea en la base de datos (@Transient).
    // Tu Frontend en Angular te seguirá enviando y pidiendo un LocalDate normal y corriente.
    @Transient
    private LocalDate fechaNacimiento;

    // =========================================================================
    // 🔓 DESCIFRADO AUTOMÁTICO AL LEER DE LA BD (Hacia Java/Angular)
    // =========================================================================
    @PostLoad
    public void decryptParticipanteData() {
        // 1. Desciframos el DNI
        if (this.dni != null) {
            this.dni = CryptoUtil.decrypt(this.dni);
        }
        // 2. Desciframos el Nombre
        if (this.nombre != null) {
            this.nombre = CryptoUtil.decrypt(this.nombre);
        }
        // 3. Desciframos la Fecha (String AES -> Convierte a LocalDate)
        if (this.fechaNacimientoCifrada != null) {
            String fechaPlano = CryptoUtil.decrypt(this.fechaNacimientoCifrada);
            try {
                this.fechaNacimiento = LocalDate.parse(fechaPlano); // Formato "YYYY-MM-DD"
            } catch (Exception e) {
                this.fechaNacimiento = null; // Salvavidas si hay datos corruptos viejos
            }
        }
    }

    // =========================================================================
    // 🔒 CIFRADO AUTOMÁTICO AL CREAR (INSERT)
    // =========================================================================
    @PrePersist
    public void onPrePersist() {
        encryptData();
    }

    // =========================================================================
    // 🔒 CIFRADO AUTOMÁTICO AL ACTUALIZAR (UPDATE)
    // =========================================================================
    @PreUpdate
    public void onPreUpdate() {
        encryptData();
    }

    // 🔄 Método privado auxiliar para aplicar el cifrado simétrico
    private void encryptData() {
        // 1. Ciframos el DNI
        if (this.dni != null) {
            this.dni = CryptoUtil.encrypt(this.dni.trim().toUpperCase());
        }
        // 2. Ciframos el Nombre
        if (this.nombre != null) {
            this.nombre = CryptoUtil.encrypt(this.nombre.trim());
        }
        // 3. Ciframos la Fecha (LocalDate -> Convierte a String AES)
        if (this.fechaNacimiento != null) {
            String fechaString = this.fechaNacimiento.toString(); // Convierte a "YYYY-MM-DD"
            this.fechaNacimientoCifrada = CryptoUtil.encrypt(fechaString);
        }
    }
}