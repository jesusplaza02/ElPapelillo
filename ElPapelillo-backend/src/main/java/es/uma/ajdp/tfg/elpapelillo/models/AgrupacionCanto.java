package es.uma.ajdp.tfg.elpapelillo.models;

import es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.enums.ModalidadCanto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "agrupacioncanto")
@PrimaryKeyJoinColumn(name = "idAgrupacion") 

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgrupacionCanto extends Agrupacion {

    @Column(name = "autorLetra")
    private String autorLetra;

    @Column(name = "autorMusica")
    private String autorMusica;

    @Column(name = "direccion") 
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad")
    private ModalidadCanto modalidad;

    public AgrupacionCanto(String nombre, String nombreUltimaParticipacion, Integer anio, 
                            CategoriaAgrupacion categoria, 
                             String autorLetra, String autorMusica, 
                            String direccion, ModalidadCanto modalidad) {
        super(); // Llama al constructor de Agrupacion
        this.setNombre(nombre);
        this.setNombreUltimaParticipacion(nombreUltimaParticipacion);
        this.setAnio(anio); 
        this.setCategoria(categoria);
        this.autorLetra = autorLetra;
        this.autorMusica = autorMusica;
        this.direccion = direccion;
        this.modalidad = modalidad;
    }
}