package es.uma.ajdp.tfg.elpapelillo.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import es.uma.ajdp.tfg.elpapelillo.models.Participacion;

public interface ParticipacionRepository extends JpaRepository<Participacion, Integer> {

    List<Participacion> findByInscripcionIdInscripcion(Integer idInscripcion);
}