package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.Concurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio JPA para la entidad Concurso.
 */
@Repository
public interface ConcursoRepository extends JpaRepository<Concurso, Long> {

    List<Concurso> findByEstado(Concurso.EstadoConcurso estado);

    List<Concurso> findByFechaInicioBetween(LocalDate desde, LocalDate hasta);

    boolean existsByNombre(String nombre);
}
