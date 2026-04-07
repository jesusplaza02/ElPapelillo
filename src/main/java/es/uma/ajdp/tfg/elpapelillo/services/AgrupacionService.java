package es.uma.ajdp.tfg.elpapelillo.services;

import es.uma.ajdp.tfg.elpapelillo.models.Agrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.Concurso;
import es.uma.ajdp.tfg.elpapelillo.repositories.AgrupacionRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.ConcursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de Agrupaciones.
 */
@Service
@RequiredArgsConstructor
public class AgrupacionService {

    private final AgrupacionRepository agrupacionRepository;
    private final ConcursoRepository concursoRepository;
    private final LogSistemaService logSistemaService;

    /**
     * Inscribe una agrupación en un concurso.
     *
     * @param agrupacion  datos de la agrupación
     * @param concursoId  ID del concurso al que se inscribe
     * @return agrupación guardada
     */
    @Transactional
    public Agrupacion inscribirAgrupacion(Agrupacion agrupacion, Long concursoId) {
        Concurso concurso = concursoRepository.findById(concursoId)
                .orElseThrow(() -> new IllegalArgumentException("Concurso no encontrado con id: " + concursoId));
        if (concurso.getEstado() != Concurso.EstadoConcurso.ABIERTO) {
            throw new IllegalStateException("El concurso no está abierto para inscripciones.");
        }
        agrupacion.setConcurso(concurso);
        Agrupacion guardada = agrupacionRepository.save(agrupacion);
        logSistemaService.registrarInfo(
                "INSCRIBIR_AGRUPACION",
                "Agrupación inscrita: " + agrupacion.getNombre() + " en concurso id=" + concursoId,
                null
        );
        return guardada;
    }

    /**
     * Obtiene todas las agrupaciones de un concurso.
     */
    @Transactional(readOnly = true)
    public List<Agrupacion> obtenerPorConcurso(Long concursoId) {
        return agrupacionRepository.findByConcursoId(concursoId);
    }

    /**
     * Busca una agrupación por su ID.
     */
    @Transactional(readOnly = true)
    public Optional<Agrupacion> buscarPorId(Long id) {
        return agrupacionRepository.findById(id);
    }

    /**
     * Obtiene todas las agrupaciones.
     */
    @Transactional(readOnly = true)
    public List<Agrupacion> obtenerTodas() {
        return agrupacionRepository.findAll();
    }

    /**
     * Elimina una agrupación por su ID.
     */
    @Transactional
    public void eliminarAgrupacion(Long id) {
        if (!agrupacionRepository.existsById(id)) {
            throw new IllegalArgumentException("Agrupación no encontrada con id: " + id);
        }
        agrupacionRepository.deleteById(id);
    }
}
