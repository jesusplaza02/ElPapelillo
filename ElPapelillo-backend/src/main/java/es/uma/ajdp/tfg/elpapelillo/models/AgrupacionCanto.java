package es.uma.ajdp.tfg.elpapelillo.models;

import es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.models.enums.ModalidadCanto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agrupaciones_canto")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgrupacionCanto extends Agrupacion {

    private String autorLetra;
    private String autorMusica;
    private String autorDireccion;
    private ModalidadCanto modalidad;

    public AgrupacionCanto(String nombre, String nombreUltimaParticipacion, Integer anio, CategoriaAgrupacion categoria, 
                       EstadoAdministrativo estadoInscripcion, Representante representante,String autorLetra, String autorMusica, ModalidadCanto modalidad) {
        this.setNombre(nombre);
        this.setNombreUltimaParticipacion(nombreUltimaParticipacion);
        this.setAnio(anio);
        this.setCategoria(categoria);
        this.setEstadoInscripcion(estadoInscripcion);
        this.setRepresentante(representante);
        this.autorLetra = autorLetra;
        this.autorMusica = autorMusica;
        this.modalidad = modalidad;
    }
}