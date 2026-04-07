package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.Representante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Representante.
 */
@Repository
public interface RepresentanteRepository extends JpaRepository<Representante, Long> {

    Optional<Representante> findByDni(String dni);

    List<Representante> findByAgrupacionId(Long agrupacionId);
}
