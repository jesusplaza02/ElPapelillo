package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.Concurso;
import es.uma.ajdp.tfg.elpapelillo.services.ConcursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para la gestión de concursos.
 * Base path: /api/concursos
 */
@RestController
@RequestMapping("/api/concursos")
@RequiredArgsConstructor
public class ConcursoController {

    private final ConcursoService concursoService;

    /**
     * GET /api/concursos — Lista todos los concursos.
     */
    @GetMapping
    public ResponseEntity<List<Concurso>> listar() {
        return ResponseEntity.ok(concursoService.obtenerTodos());
    }

    /**
     * GET /api/concursos/{id} — Obtiene un concurso por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Concurso> obtener(@PathVariable Long id) {
        return concursoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/concursos?estado={estado} — Filtra concursos por estado.
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Concurso>> listarPorEstado(@PathVariable Concurso.EstadoConcurso estado) {
        return ResponseEntity.ok(concursoService.obtenerPorEstado(estado));
    }

    /**
     * POST /api/concursos — Crea un nuevo concurso (solo ADMINISTRADOR).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Concurso> crear(@Valid @RequestBody Concurso concurso) {
        Concurso guardado = concursoService.crearConcurso(concurso);
        URI location = URI.create("/api/concursos/" + guardado.getId());
        return ResponseEntity.created(location).body(guardado);
    }

    /**
     * PATCH /api/concursos/{id}/estado — Actualiza el estado de un concurso.
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Concurso> actualizarEstado(
            @PathVariable Long id,
            @RequestParam Concurso.EstadoConcurso estado) {
        Concurso actualizado = concursoService.actualizarEstado(id, estado);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * DELETE /api/concursos/{id} — Elimina un concurso (solo ADMINISTRADOR).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        concursoService.eliminarConcurso(id);
        return ResponseEntity.noContent().build();
    }
}
