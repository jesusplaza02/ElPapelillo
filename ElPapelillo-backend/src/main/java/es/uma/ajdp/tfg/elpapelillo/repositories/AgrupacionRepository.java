package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.Agrupacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgrupacionRepository extends JpaRepository<Agrupacion, Integer> {

    // Cambiado a Long para ser consistente con la entidad
    Optional<Agrupacion> findById( Integer id);

    List<Agrupacion> findByRepresentante_IdUsuario(Integer idUsuario);
}