package es.uma.ajdp.tfg.elpapelillo.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.uma.ajdp.tfg.elpapelillo.models.Inscripcion;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.repositories.InscripcionRepository;

@Service
public class InscripcionService {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    public List<Inscripcion> obtenerInscripcionesPorRepresentante(Integer idRepresentante) {
        return inscripcionRepository.findByAgrupacion_Representante_IdUsuario(idRepresentante);
    }

    // 2. Crear una nueva inscripción con la lógica de negocio aplicada
    public Inscripcion crearInscripcion(Inscripcion nuevaInscripcion) {
        nuevaInscripcion.setFechaInscripcion(LocalDateTime.now());
        nuevaInscripcion.setEstadoInscripcion(EstadoAdministrativo.PENDIENTE);
        
        return inscripcionRepository.save(nuevaInscripcion);
    }

    // --- NUEVO MÉTODO PARA CARGAR LOS DATOS EN EL DETALLE DEL CONCURSO ---
    public List<Inscripcion> obtenerInscripcionesPorConcurso(Integer idConcurso) {
        // Llama al repositorio usando la relación que tiene Inscripcion con Concurso
        return inscripcionRepository.findByConcursoIdConcurso(idConcurso);
    }
}