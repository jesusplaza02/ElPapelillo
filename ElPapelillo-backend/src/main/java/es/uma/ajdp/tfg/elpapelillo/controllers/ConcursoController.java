package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.Concurso;
import es.uma.ajdp.tfg.elpapelillo.services.ConcursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/concursos")
@CrossOrigin(origins = "http://localhost:4200")
public class ConcursoController {

    @Autowired
    private ConcursoService concursoService;

    /**
     * Endpoint principal para la tabla de concursos.
     * Filtra automáticamente: 
     * - SYSADMIN: Ve todos.
     * - ADMIN/SUPERADMIN: Solo los de su organización.
     */
    @GetMapping("/mis-concursos/{idUsuarioActual}")
    public ResponseEntity<List<Concurso>> getConcursosPorRol(@PathVariable Integer idUsuarioActual) {
        try {
            List<Concurso> concursos = concursoService.listarConcursosSegunRol(idUsuarioActual);
            return ResponseEntity.ok(concursos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para obtener solo concursos activos (útil para selects o landing page).
     */
    @GetMapping("/activos")
    public ResponseEntity<List<Concurso>> getActivos() {
        return ResponseEntity.ok(concursoService.findActivos());
    }

    /**
     * Ejemplo de cómo podrías tener el detalle de un concurso específico.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Concurso> getById(@PathVariable Integer id) {
        // Asumiendo que añades este método al service después
        return concursoService.findById(id) 
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Concurso> crearConcurso(@RequestBody Concurso concurso) {
        try {
            Concurso nuevo = concursoService.guardar(concurso);
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualizar un concurso existente (PUT)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Concurso> actualizarConcurso(@PathVariable Integer id, @RequestBody Concurso concurso) {
        try {
            // Aseguramos que el objeto tenga el ID correcto antes de guardar
            concurso.setIdConcurso(id); 
            Concurso actualizado = concursoService.guardar(concurso);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            concursoService.eliminarConcurso(id);
            return ResponseEntity.ok().body("{\"message\": \"Concurso eliminado permanentemente\"}");
        } catch (RuntimeException e) {
            // Enviamos el mensaje de error de las reglas (ej: "tiene agrupaciones")
            return ResponseEntity.status(400).body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
}