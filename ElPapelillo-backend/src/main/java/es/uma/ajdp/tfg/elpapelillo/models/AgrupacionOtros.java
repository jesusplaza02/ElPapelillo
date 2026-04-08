package es.uma.ajdp.tfg.elpapelillo.models;

import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agrupaciones_otros")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgrupacionOtros extends Agrupacion {

    private String comentariosDestacables;

    public AgrupacionOtros(String nombre, String nombreUltimaParticipacion, Integer anio, CategoriaAgrupacion categoria, 
                       EstadoAdministrativo estadoInscripcion, Representante representante ,String comentariosDestacables) {
        this.setNombre(nombre);
        this.setNombreUltimaParticipacion(nombreUltimaParticipacion);
        this.setAnio(anio);
        this.setCategoria(categoria);
        this.setEstadoInscripcion(estadoInscripcion);
        this.setRepresentante(representante);
        this.comentariosDestacables = comentariosDestacables;
    }
}