package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.Concurso;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoConcurso;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConcursoRepository extends JpaRepository<Concurso, Integer> {
    // Si en tu base de datos la tabla concursos tiene una columna 'estado'
    List<Concurso> findByEstadoConcurso(EstadoConcurso estado);
}