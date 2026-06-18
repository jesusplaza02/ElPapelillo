package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.Concurso;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoConcurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConcursoRepository extends JpaRepository<Concurso, Integer> {
    List<Concurso> findByEstadoConcurso(EstadoConcurso estado);
    @Query("SELECT c FROM Concurso c WHERE :idOrg IS NULL OR c.organizacion.idOrganizacion = :idOrg")
    List<Concurso> findByOrganizacionOpcional(@Param("idOrg") Integer idOrg);
}

