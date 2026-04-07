package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.Agrupacion;
import es.uma.ajdp.tfg.elpapelillo.services.AgrupacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para la gestión de agrupaciones.
 * Base path: /api/agrupaciones
 */
@RestController
@RequestMapping("/api/agrupaciones")
@RequiredArgsConstructor
public class AgrupacionController {

    private final AgrupacionService agrupacionService;

    /**
     * GET /api/agrupaciones — Lista todas las agrupaciones.
     */
    @GetMapping
    public ResponseEntity<List<Agrupacion>> listar() {
        return ResponseEntity.ok(agrupacionService.obtenerTodas());
    }

    /**
     * GET /api/agrupaciones/{id} — Obtiene una agrupación por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Agrupacion> obtener(@PathVariable Long id) {
        return agrupacionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/agrupaciones/concurso/{concursoId} — Lista agrupaciones de un concurso.
     */
    @GetMapping("/concurso/{concursoId}")
    public ResponseEntity<List<Agrupacion>> listarPorConcurso(@PathVariable Long concursoId) {
        return ResponseEntity.ok(agrupacionService.obtenerPorConcurso(concursoId));
    }

    /**
     * POST /api/agrupaciones/concurso/{concursoId} — Inscribe una agrupación en un concurso.
     */
    @PostMapping("/concurso/{concursoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'REPRESENTANTE')")
    public ResponseEntity<Agrupacion> inscribir(
            @PathVariable Long concursoId,
            @Valid @RequestBody Agrupacion agrupacion) {
        Agrupacion guardada = agrupacionService.inscribirAgrupacion(agrupacion, concursoId);
        URI location = URI.create("/api/agrupaciones/" + guardada.getId());
        return ResponseEntity.created(location).body(guardada);
    }

    /**
     * DELETE /api/agrupaciones/{id} — Elimina una agrupación (solo ADMINISTRADOR).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        agrupacionService.eliminarAgrupacion(id);
        return ResponseEntity.noContent().build();
    }
}
