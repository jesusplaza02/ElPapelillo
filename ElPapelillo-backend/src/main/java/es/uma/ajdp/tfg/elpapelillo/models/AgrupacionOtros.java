package es.uma.ajdp.tfg.elpapelillo.models;


import es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agrupacionotros")
@PrimaryKeyJoinColumn(name = "idAgrupacion")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgrupacionOtros extends Agrupacion {

    private String comentariosDestacables;

    public AgrupacionOtros(String nombre, String nombreUltimaParticipacion, Integer anio, CategoriaAgrupacion categoria, 
                         String comentariosDestacables) {
        this.setNombre(nombre);
        this.setNombreUltimaParticipacion(nombreUltimaParticipacion);
        this.setAnio(anio);
        this.setCategoria(categoria);
        this.comentariosDestacables = comentariosDestacables;
    }
}