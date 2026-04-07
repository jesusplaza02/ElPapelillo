package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.LogSistema;
import es.uma.ajdp.tfg.elpapelillo.services.LogSistemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la consulta de logs del sistema.
 * Base path: /api/logs
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class LogSistemaController {

    private final LogSistemaService logSistemaService;

    /**
     * GET /api/logs — Obtiene los últimos 50 logs del sistema.
     */
    @GetMapping
    public ResponseEntity<List<LogSistema>> listarUltimos() {
        return ResponseEntity.ok(logSistemaService.obtenerUltimosLogs());
    }

    /**
     * GET /api/logs/usuario/{usuarioId} — Obtiene los logs de un usuario.
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<LogSistema>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(logSistemaService.obtenerLogsPorUsuario(usuarioId));
    }

    /**
     * GET /api/logs/nivel/{nivel} — Obtiene los logs filtrados por nivel.
     */
    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<List<LogSistema>> listarPorNivel(@PathVariable LogSistema.NivelLog nivel) {
        return ResponseEntity.ok(logSistemaService.obtenerLogsPorNivel(nivel));
    }
}
