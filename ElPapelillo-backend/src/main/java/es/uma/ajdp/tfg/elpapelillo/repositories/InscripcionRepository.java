package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {
    
    // Método útil para que el representante vea sus inscripciones
    List<Inscripcion> findByAgrupacionIdAgrupacion(Integer idAgrupacion);
    
    // Método útil para que el administrador vea inscripciones de un concurso concreto
    List<Inscripcion> findByConcursoIdConcurso(Integer idConcurso);

    List<Inscripcion> findByAgrupacion_Representante_IdUsuario(Integer idRepresentante);

    List<Inscripcion> findByIdInscripcion(Integer idInscripcion);

}