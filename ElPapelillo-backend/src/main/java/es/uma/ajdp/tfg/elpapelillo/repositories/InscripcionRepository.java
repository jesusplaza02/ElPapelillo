package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {
    
    // Método útil para que el representante vea sus inscripciones
    List<Inscripcion> findByAgrupacionIdAgrupacion(Integer idAgrupacion);
    
    @Query("SELECT i FROM Inscripcion i WHERE i.concurso.idConcurso = :idConcurso")
    List<Inscripcion> findByConcursoIdConcurso(@Param("idConcurso") Integer idConcurso);

    List<Inscripcion> findByAgrupacion_Representante_IdUsuario(Integer idRepresentante);

    List<Inscripcion> findByIdInscripcion(Integer idInscripcion);

   @Query(value = "SELECT * FROM inscripciones WHERE id_concurso = :idConcurso", nativeQuery = true)
    List<Inscripcion> findByConcursoIdManual(@Param("idConcurso") Long idConcurso);

    boolean existsByConcurso_IdConcursoAndAgrupacion_NombreIgnoreCase(Integer idConcurso, String nombreAgrupacion);


}