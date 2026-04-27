package es.uma.ajdp.tfg.elpapelillo.models.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import es.uma.ajdp.tfg.elpapelillo.models.enums.TipoConcurso;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgrupacionDTO {
    // ID de la agrupación (null si es nueva)
    private Integer idAgrupacion;

    // Campos comunes
    private String nombre;
    private String nombreUltimaParticipacion;
    private String categoria; 
    private Integer anio; 
    private Integer idConcurso; 
    private Integer idRepresentante;
    private TipoConcurso tipoConcurso; 

    // Campos específicos Canto
    private String autorLetra;
    private String autorMusica;
    private String direccion;

    // Campos específicos Drag / Dioses
    private String nombreArtisticoDrag;
    private String disenador; 
    private String modelo;

    // Campos Otros
    private String comentariosDestacables;
}