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

    // ===================================================================
    // NUEVO: Obtener una inscripción por su ID (Elimina el primer error)
    // ===================================================================
    public Inscripcion obtenerInscripcionPorId(Integer id) {
        return inscripcionRepository.findById(id).orElse(null);
    }

    // ===================================================================
    // NUEVO: Modificar el estado transformando String a Enum (Elimina el segundo error)
    // ===================================================================
    public Inscripcion cambiarEstadoInscripcion(Integer id, String nuevoEstadoStr) {
        // 1. Buscamos la inscripción en la base de datos
        Inscripcion ins = inscripcionRepository.findById(id).orElse(null);
        
        if (ins != null && nuevoEstadoStr != null) {
            try {
                // 2. Convertimos el String ("APROBADO" / "RECHAZADO") que viene del Front en tu Enum de Java
                EstadoAdministrativo nuevoEstadoEnum = EstadoAdministrativo.valueOf(nuevoEstadoStr.toUpperCase());
                
                // 3. Asignamos el estado mapeado y guardamos en la BD
                ins.setEstadoInscripcion(nuevoEstadoEnum);
                return inscripcionRepository.save(ins);
                
            } catch (IllegalArgumentException e) {
                System.err.println("El estado enviado '" + nuevoEstadoStr + "' no coincide con ningún valor del Enum EstadoAdministrativo.");
            }
        }
        return null;
    }
}