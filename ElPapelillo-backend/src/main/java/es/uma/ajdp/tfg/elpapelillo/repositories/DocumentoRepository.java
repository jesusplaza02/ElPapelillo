package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.Documento;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Integer> {
    
    List<Documento> findByEstado(EstadoAdministrativo estado);

    List<Documento> findByInscripcionIdInscripcion(Integer idInscripcion);
}