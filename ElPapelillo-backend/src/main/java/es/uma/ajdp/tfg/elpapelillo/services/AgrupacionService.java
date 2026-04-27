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

    /**
     * Busca agrupaciones base por el ID del representante.
     */
    public List<Agrupacion> findBaseByRepresentanteId(Integer idRep) {
        return agrupacionRepository.findByRepresentante_IdUsuario(idRep);
    }

    /**
     * Obtiene las inscripciones de todas las agrupaciones de un representante.
     */
    public List<Inscripcion> findInscripcionesPorRepresentante(Integer idRep) {
        return inscripcionRepository.findByAgrupacion_Representante_IdUsuario(idRep);
    }

    /**
     * Guarda o actualiza una agrupación y crea una inscripción si se proporciona un ID de concurso.
     */
    @Transactional
    public Agrupacion guardarDesdeDto(AgrupacionDTO dto) {
        Agrupacion agrupacion;

        // 1. Lógica de Actualización o Creación
        if (dto.getIdAgrupacion() != null) {
            agrupacion = agrupacionRepository.findById(dto.getIdAgrupacion())
                .orElseThrow(() -> new RuntimeException("Agrupación no encontrada con ID: " + dto.getIdAgrupacion()));
        } else {
            // Instanciamos el tipo concreto según el tipo de concurso
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

        // 2. Mapeo de campos comunes
        agrupacion.setNombre(dto.getNombre());
        agrupacion.setNombreUltimaParticipacion(dto.getNombreUltimaParticipacion());
        agrupacion.setAnio(dto.getAnio());
        agrupacion.setTipoConcurso(dto.getTipoConcurso()); // Aseguramos que se guarde el Enum
        
        if (dto.getCategoria() != null) {
            agrupacion.setCategoria(CategoriaAgrupacion.valueOf(dto.getCategoria().toUpperCase()));
        }

        // 3. Asignación del Representante (Evitando el ClassCastException)
        if (dto.getIdRepresentante() != null) {
            Representante rep = representanteRepository.findById(dto.getIdRepresentante())
                .orElseThrow(() -> new RuntimeException("Representante no encontrado con ID: " + dto.getIdRepresentante()));
            agrupacion.setRepresentante(rep);
        }

        // Guardamos la agrupación primero para tener el ID generado
        agrupacion = agrupacionRepository.save(agrupacion);

        // 4. Crear Inscripción automática si viene un concurso en el DTO
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