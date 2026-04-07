package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.Agrupacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Agrupacion.
 */
@Repository
public interface AgrupacionRepository extends JpaRepository<Agrupacion, Long> {

    List<Agrupacion> findByConcursoId(Long concursoId);

    List<Agrupacion> findByModalidad(Agrupacion.Modalidad modalidad);

    List<Agrupacion> findByMunicipio(String municipio);
}
