package es.uma.ajdp.tfg.elpapelillo.services;

import es.uma.ajdp.tfg.elpapelillo.models.*;
import es.uma.ajdp.tfg.elpapelillo.models.dtos.AgrupacionDTO;
import es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgrupacionService {

    @Autowired
    private AgrupacionRepository agrupacionRepository;

    @Autowired
    private RepresentanteRepository representanteRepository; // Cambiado de UsuarioRepository para evitar errores de casting

    @Autowired
    private ConcursoRepository concursoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    public List<Agrupacion> findBaseByRepresentanteId(Integer idRep) {
        return agrupacionRepository.findByRepresentante_IdUsuario(idRep);
    }

    public List<Inscripcion> findInscripcionesPorRepresentante(Integer idRep) {
        return inscripcionRepository.findByAgrupacion_Representante_IdUsuario(idRep);
    }

    @Transactional
    public Agrupacion guardarDesdeDto(AgrupacionDTO dto) {
        Agrupacion agrupacion;

        if (dto.getIdAgrupacion() != null) {
            agrupacion = agrupacionRepository.findById(dto.getIdAgrupacion())
                .orElseThrow(() -> new RuntimeException("Agrupación no encontrada con ID: " + dto.getIdAgrupacion()));
        } else {
            String tipoStr = (dto.getTipoConcurso() != null) ? dto.getTipoConcurso().toString().toUpperCase() : "";

            switch (tipoStr) {
                case "CANTO":
                    AgrupacionCanto canto = new AgrupacionCanto();
                    canto.setAutorLetra(dto.getAutorLetra());
                    canto.setAutorMusica(dto.getAutorMusica());
                    canto.setDireccion(dto.getDireccion());
                    agrupacion = canto;
                    break;
                case "DRAG":
                    AgrupacionDrag drag = new AgrupacionDrag();
                    drag.setNombreArtisticoDrag(dto.getNombreArtisticoDrag());
                    drag.setDisenador(dto.getDisenador());
                    agrupacion = drag;
                    break;
                case "DIOSES":
                    AgrupacionDioses dioses = new AgrupacionDioses();
                    dioses.setModelo(dto.getModelo());
                    dioses.setDisenador(dto.getDisenador());
                    agrupacion = dioses;
                    break;
                default:
                    agrupacion = new AgrupacionOtros();
                    break;
            }
        }

        agrupacion.setNombre(dto.getNombre());
        agrupacion.setNombreUltimaParticipacion(dto.getNombreUltimaParticipacion());
        agrupacion.setAnio(dto.getAnio());
        agrupacion.setTipoConcurso(dto.getTipoConcurso());
        
        if (dto.getCategoria() != null) {
            agrupacion.setCategoria(CategoriaAgrupacion.valueOf(dto.getCategoria().toUpperCase()));
        }

        if (dto.getIdRepresentante() != null) {
            Representante rep = representanteRepository.findById(dto.getIdRepresentante())
                .orElseThrow(() -> new RuntimeException("Representante no encontrado con ID: " + dto.getIdRepresentante()));
            agrupacion.setRepresentante(rep);
        }

        agrupacion = agrupacionRepository.save(agrupacion);

        if (dto.getIdConcurso() != null) {
            final Agrupacion agFinal = agrupacion;
            concursoRepository.findById(dto.getIdConcurso())
                .ifPresent(concurso -> {
                    Inscripcion ins = new Inscripcion();
                    ins.setAgrupacion(agFinal);
                    ins.setConcurso(concurso);
                    ins.setFechaInscripcion(LocalDateTime.now());
                    ins.setEstadoInscripcion(EstadoAdministrativo.PENDIENTE);
                    inscripcionRepository.save(ins);
                });
        }

        return agrupacion;
    }
}