package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Administrador.
 */
@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    List<Administrador> findByNivelAcceso(int nivelAcceso);
}
