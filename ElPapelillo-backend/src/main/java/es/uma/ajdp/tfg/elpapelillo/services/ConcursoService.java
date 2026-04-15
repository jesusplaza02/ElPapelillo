package es.uma.ajdp.tfg.elpapelillo.services;

import es.uma.ajdp.tfg.elpapelillo.models.Concurso;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoConcurso;
import es.uma.ajdp.tfg.elpapelillo.repositories.ConcursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ConcursoService {

    @Autowired
    private ConcursoRepository concursoRepository;

    public List<Concurso> findActivos() {
        // Opción A: Si tienes columna estado en la BD
        //return concursoRepository.findByEstadoConcurso(EstadoConcurso.ACTIVO);
        
        // Opción B: Si aún no tienes estados y quieres probar que el select cargue algo:
        return concursoRepository.findAll(); 
    }
}