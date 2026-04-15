package es.uma.ajdp.tfg.elpapelillo.models.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgrupacionDTO {
    // Campos de la tabla 'agrupacion'
    private String nombre;
    private String nombreUltimaParticipacion;
    private String categoria;      // ADULTO, JUVENIL, INFANTIL
    private Integer anio;          // El año que calculamos en Angular
    private Long idConcurso;       
    private Integer idRepresentante;
    private String tipoConcurso;   // CANTO, DRAG, DIOSES

    // Campos de 'agrupacioncanto'
    private String autorLetra;
    private String autorMusica;
    private String direccion;

    // Campos de 'agrupaciondrag'
    private String nombreArtisticoDrag;
    private String disenador; 

    // Campos de 'agrupaciondioses'
    private String modelo;

    // Campos de 'agrupacionOtro'
    private String comentariosDestacables;
}