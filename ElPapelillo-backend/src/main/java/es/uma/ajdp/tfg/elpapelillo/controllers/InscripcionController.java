package es.uma.ajdp.tfg.elpapelillo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uma.ajdp.tfg.elpapelillo.models.Inscripcion;
import es.uma.ajdp.tfg.elpapelillo.services.InscripcionService;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @GetMapping("/representante/{idRepresentante}")
    public ResponseEntity<List<Inscripcion>> getInscripcionesRepresentante(@PathVariable Integer idRepresentante) {
        List<Inscripcion> inscripciones = inscripcionService.obtenerInscripcionesPorRepresentante(idRepresentante);
        return ResponseEntity.ok(inscripciones);
    }

    // 2. Crear una nueva inscripción
    @PostMapping
    public ResponseEntity<Inscripcion> crearInscripcion(@RequestBody Inscripcion nuevaInscripcion) {
        Inscripcion guardada = inscripcionService.crearInscripcion(nuevaInscripcion);
        return ResponseEntity.ok(guardada);
    }
}