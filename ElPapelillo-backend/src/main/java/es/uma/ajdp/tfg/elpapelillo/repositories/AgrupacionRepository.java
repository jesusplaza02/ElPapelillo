package es.uma.ajdp.tfg.elpapelillo.repositories;


import es.uma.ajdp.tfg.elpapelillo.models.Agrupacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgrupacionRepository extends JpaRepository<Agrupacion, Long> {

    /**
     * Para el TRASPASO: Busca todas las agrupaciones que pertenecen a un representante concreto.
     */
    List<Agrupacion> findByRepresentanteId(Long representanteId);
}