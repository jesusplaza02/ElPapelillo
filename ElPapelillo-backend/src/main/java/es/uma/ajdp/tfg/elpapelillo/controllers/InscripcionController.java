package es.uma.ajdp.tfg.elpapelillo.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uma.ajdp.tfg.elpapelillo.models.Inscripcion;
import es.uma.ajdp.tfg.elpapelillo.services.InscripcionService;

@RestController
@RequestMapping("/api/inscripciones")
@CrossOrigin(origins = "http://localhost:4200") // Evita problemas de CORS con Angular
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    // 1. Obtener inscripciones de un representante
    @GetMapping("/representante/{idRepresentante}")
    public ResponseEntity<List<Inscripcion>> getInscripcionesRepresentante(@PathVariable Integer idRepresentante) {
        List<Inscripcion> inscripciones = inscripcionService.obtenerInscripcionesPorRepresentante(idRepresentante);
        return ResponseEntity.ok(inscripciones);
    }

    // 2. Obtener inscripciones de un concurso concreto
    @GetMapping("/concurso/{idConcurso}")
    public ResponseEntity<List<Inscripcion>> getInscripcionesConcurso(@PathVariable Integer idConcurso) {
        List<Inscripcion> inscripciones = inscripcionService.obtenerInscripcionesPorConcurso(idConcurso);
        return ResponseEntity.ok(inscripciones);
    }

    // 3. Crear una nueva inscripción
    @PostMapping
    public ResponseEntity<Inscripcion> crearInscripcion(@RequestBody Inscripcion nuevaInscripcion) {
        Inscripcion guardada = inscripcionService.crearInscripcion(nuevaInscripcion);
        return ResponseEntity.ok(guardada);
    }

    // ===================================================================
    // NUEVO: Obtener una inscripción individual por su ID para el detalle
    // ===================================================================
    @GetMapping("/{id}")
    public ResponseEntity<Inscripcion> getInscripcionPorId(@PathVariable Integer id) {
        Inscripcion inscripcion = inscripcionService.obtenerInscripcionPorId(id);
        
        if (inscripcion != null) {
            return ResponseEntity.ok(inscripcion);
        } else {
            return ResponseEntity.notFound().build(); // Devuelve 404 si el ID no existe en la BD
        }
    }

    // ===================================================================
    // NUEVO: Actualizar el estado de la inscripción (APROBADO/RECHAZADO)
    // ===================================================================
    @PutMapping("/{id}/estado")
    public ResponseEntity<Inscripcion> actualizarEstadoInscripcion(
            @PathVariable Integer id, 
            @RequestBody Map<String, String> body) {
        
        String nuevoEstado = body.get("estado");
        Inscripcion actualizada = inscripcionService.cambiarEstadoInscripcion(id, nuevoEstado);
        
        if (actualizada != null) {
            return ResponseEntity.ok(actualizada);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}