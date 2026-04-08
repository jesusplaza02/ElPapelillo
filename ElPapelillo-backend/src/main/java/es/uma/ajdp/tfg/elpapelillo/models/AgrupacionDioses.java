package es.uma.ajdp.tfg.elpapelillo.models;

import es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.models.enums.ModalidadDioses;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agrupaciones_dioses")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgrupacionDioses extends Agrupacion {

    private String tallerCostura;
    private String maquillador;
    private String peluquero;
    private ModalidadDioses modalidad;

    public AgrupacionDioses(String nombre, String nombreUltimaParticipacion, Integer anio, CategoriaAgrupacion categoria, 
                       EstadoAdministrativo estadoInscripcion, Representante representante, String taller, String maquillador, String peluquero, ModalidadDioses modalidad) {
        this.setNombre(nombre);
        this.setNombreUltimaParticipacion(nombreUltimaParticipacion);
        this.setAnio(anio);
        this.setCategoria(categoria);
        this.setEstadoInscripcion(estadoInscripcion);
        this.setRepresentante(representante);
        this.tallerCostura = taller;
        this.maquillador = maquillador;
        this.peluquero = peluquero;
        this.modalidad = modalidad;
    }
}