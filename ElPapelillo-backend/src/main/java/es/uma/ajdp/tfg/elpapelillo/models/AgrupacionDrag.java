package es.uma.ajdp.tfg.elpapelillo.models;

import es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agrupaciones_drag")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgrupacionDrag extends Agrupacion {

    private String nombreArtistico;
    private String diseñadorGala;
    private String coreografo;

    public AgrupacionDrag(String nombre, String nombreUltimaParticipacion, Integer anio, CategoriaAgrupacion categoria, 
                       EstadoAdministrativo estadoInscripcion, Representante representante, String nombreArtistico, String diseñador) {
        this.setNombre(nombre);
        this.setNombreUltimaParticipacion(nombreUltimaParticipacion);
        this.setAnio(anio);
        this.setCategoria(categoria);
        this.setEstadoInscripcion(estadoInscripcion);
        this.setRepresentante(representante);
        this.nombreArtistico = nombreArtistico;
        this.diseñadorGala = diseñador;
    }
}