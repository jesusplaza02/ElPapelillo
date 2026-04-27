package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.Agrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.Inscripcion;
import es.uma.ajdp.tfg.elpapelillo.services.AgrupacionService;
import es.uma.ajdp.tfg.elpapelillo.models.dtos.AgrupacionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agrupaciones")
@CrossOrigin(origins = "http://localhost:4200") 
public class AgrupacionController {

    @Autowired
    private AgrupacionService agrupacionService;

    /**
     * Devuelve las INSCRIPCIONES (para el grid de tarjetas).
     * Ruta: GET /api/agrupaciones/representante/{id}
     * Cambiamos el retorno a Inscripcion para que Angular tenga el ESTADO.
     */
    @GetMapping("/representante/{id}")
    public List<Inscripcion> getInscripciones(@PathVariable Integer id) {
        return agrupacionService.findInscripcionesPorRepresentante(id);
    }

    /**
     * Devuelve las agrupaciones BASE (para el selector de "Existente").
     * Ruta: GET /api/agrupaciones/base/{id}
     */
    @GetMapping("/base/{id}")
    public List<Agrupacion> getAgrupacionesBase(@PathVariable Integer id) {
        return agrupacionService.findBaseByRepresentanteId(id);
    }

    /**
     * Procesa el guardado de una inscripción (Nueva o Existente).
     */
    @PostMapping
    public ResponseEntity<Agrupacion> guardar(@RequestBody AgrupacionDTO dto) {
        try {
            Agrupacion guardada = agrupacionService.guardarDesdeDto(dto);
            return (guardada != null) 
                ? ResponseEntity.ok(guardada) 
                : ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al guardar agrupación: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}