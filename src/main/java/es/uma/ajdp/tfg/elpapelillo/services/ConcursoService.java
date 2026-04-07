package es.uma.ajdp.tfg.elpapelillo.services;

import es.uma.ajdp.tfg.elpapelillo.models.Concurso;
import es.uma.ajdp.tfg.elpapelillo.repositories.ConcursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de Concursos.
 */
@Service
@RequiredArgsConstructor
public class ConcursoService {

    private final ConcursoRepository concursoRepository;
    private final LogSistemaService logSistemaService;

    /**
     * Crea un nuevo concurso.
     *
     * @param concurso datos del concurso
     * @return concurso guardado
     */
    @Transactional
    public Concurso crearConcurso(Concurso concurso) {
        if (concursoRepository.existsByNombre(concurso.getNombre())) {
            throw new IllegalArgumentException("Ya existe un concurso con el nombre: " + concurso.getNombre());
        }
        Concurso guardado = concursoRepository.save(concurso);
        logSistemaService.registrarInfo(
                "CREAR_CONCURSO",
                "Concurso creado: " + concurso.getNombre(),
                null
        );
        return guardado;
    }

    /**
     * Obtiene todos los concursos.
     */
    @Transactional(readOnly = true)
    public List<Concurso> obtenerTodos() {
        return concursoRepository.findAll();
    }

    /**
     * Busca un concurso por su ID.
     */
    @Transactional(readOnly = true)
    public Optional<Concurso> buscarPorId(Long id) {
        return concursoRepository.findById(id);
    }

    /**
     * Obtiene los concursos filtrados por estado.
     */
    @Transactional(readOnly = true)
    public List<Concurso> obtenerPorEstado(Concurso.EstadoConcurso estado) {
        return concursoRepository.findByEstado(estado);
    }

    /**
     * Actualiza el estado de un concurso.
     *
     * @param id     identificador del concurso
     * @param estado nuevo estado
     * @return concurso actualizado
     */
    @Transactional
    public Concurso actualizarEstado(Long id, Concurso.EstadoConcurso estado) {
        Concurso concurso = concursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Concurso no encontrado con id: " + id));
        concurso.setEstado(estado);
        return concursoRepository.save(concurso);
    }

    /**
     * Elimina un concurso por su ID.
     *
     * @param id identificador del concurso
     */
    @Transactional
    public void eliminarConcurso(Long id) {
        if (!concursoRepository.existsById(id)) {
            throw new IllegalArgumentException("Concurso no encontrado con id: " + id);
        }
        concursoRepository.deleteById(id);
    }
}
