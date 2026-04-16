package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.Agrupacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface AgrupacionRepository extends JpaRepository<Agrupacion, Integer> {
    @Query("SELECT a FROM Agrupacion a WHERE a.representante.id = :id")
    List<Agrupacion> findByRepresentanteId(@Param("id") Integer id);
    //List<Agrupacion> findByRepresentante_IdUsuario(Integer idABorrar);

}