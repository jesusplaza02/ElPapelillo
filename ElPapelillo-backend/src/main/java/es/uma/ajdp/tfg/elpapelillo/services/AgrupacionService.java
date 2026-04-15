package es.uma.ajdp.tfg.elpapelillo.services;

import es.uma.ajdp.tfg.elpapelillo.models.Agrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.AgrupacionCanto;
import es.uma.ajdp.tfg.elpapelillo.models.AgrupacionDioses;
import es.uma.ajdp.tfg.elpapelillo.models.AgrupacionDrag;
import es.uma.ajdp.tfg.elpapelillo.models.AgrupacionOtros;
import es.uma.ajdp.tfg.elpapelillo.models.dtos.AgrupacionDTO;
import es.uma.ajdp.tfg.elpapelillo.models.enums.TipoConcurso;
import es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.repositories.AgrupacionRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.RepresentanteRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.ConcursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AgrupacionService {

    @Autowired
    private AgrupacionRepository agrupacionRepository;

    @Autowired
    private RepresentanteRepository representanteRepository;

    @Autowired
    private ConcursoRepository concursoRepository;

    public List<Agrupacion> findByRepresentanteId(Integer idRep) {
        return agrupacionRepository.findByRepresentanteId(idRep);
    }

   @Transactional
public Agrupacion guardarDesdeDto(AgrupacionDTO dto) {
    // Evitamos el error "may not have been initialized"
    Agrupacion agrupacion = null; 
    String tipoStr = (dto.getTipoConcurso() != null) ? dto.getTipoConcurso().toUpperCase() : "";

    // Instanciación correcta
    if (tipoStr.equals("CANTO")) {
        AgrupacionCanto canto = new AgrupacionCanto();
        canto.setAutorLetra(dto.getAutorLetra());
        canto.setAutorMusica(dto.getAutorMusica());
        canto.setDireccion(dto.getDireccion());
        agrupacion = canto;
    } else if (tipoStr.equals("DRAG")) {
        AgrupacionDrag drag = new AgrupacionDrag();
        drag.setNombreArtisticoDrag(dto.getNombreArtisticoDrag());
        drag.setDisenador(dto.getDisenador());
        agrupacion = drag;
    } else if (tipoStr.equals("DIOSES")) {
        AgrupacionDioses dioses = new AgrupacionDioses();
        dioses.setModelo(dto.getModelo());
        dioses.setDisenador(dto.getDisenador());
        agrupacion = dioses;
    } else if (tipoStr.equals("OTRO")) {
        AgrupacionOtros otros = new AgrupacionOtros();
        otros.setComentariosDestacables(dto.getComentariosDestacables());
        agrupacion = otros;
    }

    if (agrupacion != null) {
        agrupacion.setNombre(dto.getNombre());
        agrupacion.setNombreUltimaParticipacion(dto.getNombreUltimaParticipacion());
        agrupacion.setAnio(dto.getAnio());

        // Conversión de ID de Concurso (Long a Integer)
        if (dto.getIdConcurso() != null) {
            concursoRepository.findById(dto.getIdConcurso().intValue())
                .ifPresent(agrupacion::setConcurso);
        }
        if (dto.getIdRepresentante() != null) {
            representanteRepository.findById(dto.getIdRepresentante().longValue())
                .ifPresent(agrupacion::setRepresentante);
        }

        try {
            // Nombres de Enums corregidos según tus capturas
            agrupacion.setTipoConcurso(TipoConcurso.valueOf(tipoStr));
            agrupacion.setCategoria(CategoriaAgrupacion.valueOf(dto.getCategoria().toUpperCase()));
            agrupacion.setEstadoInscripcion(EstadoAdministrativo.PENDIENTE);
        } catch (Exception e) {
            System.err.println("Error Enums: " + e.getMessage());
        }

        return agrupacionRepository.save(agrupacion);
    }
    return null;
}
}