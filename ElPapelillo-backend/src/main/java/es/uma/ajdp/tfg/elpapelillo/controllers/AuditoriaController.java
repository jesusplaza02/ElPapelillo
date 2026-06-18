package es.uma.ajdp.tfg.elpapelillo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.uma.ajdp.tfg.elpapelillo.models.Administrador;
import es.uma.ajdp.tfg.elpapelillo.models.LogAuditoria;
import es.uma.ajdp.tfg.elpapelillo.repositories.AdministradorRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.LogAuditoriaRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(origins = "http://localhost:4200") 
public class AuditoriaController {

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @GetMapping
    public ResponseEntity<?> obtenerLogsAuditoria(@RequestParam("idUsuarioActual") Integer idUsuarioActual) {
        try {
            Optional<Administrador> adminOpt = administradorRepository.findById(idUsuarioActual);
            if (adminOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: Usuario no válido o inexistente."));
            }

            Administrador adminRequest = adminOpt.get();
            String rol = adminRequest.getRol() != null ? adminRequest.getRol().toString().toUpperCase() : "";

            // Caso SYSADMIN: Vía libre total para monitorizar todo el sistema
            if (rol.contains("SYSADMIN")) {
                List<LogAuditoria> todosLosLogs = logAuditoriaRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha"));
                return ResponseEntity.ok(todosLosLogs);
            }

            // Caso Administradores de Organización: Se restringe a los miembros de su entorno
            if (adminRequest.getOrganizacion() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: Tu cuenta no está asociada a ninguna organización corporativa."));
            }

            List<LogAuditoria> logsFiltrados = logAuditoriaRepository.findByOrganizacionDeUsuario(idUsuarioActual);
            return ResponseEntity.ok(logsFiltrados);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al recuperar los datos de auditoría: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> crearLogAuditoria(@RequestBody Map<String, Object> payload) {
        try {
            LogAuditoria nuevoLog = new LogAuditoria();
            nuevoLog.setAccion((String) payload.get("accion"));
            nuevoLog.setDescripcion((String) payload.get("descripcion"));
            nuevoLog.setFecha(java.time.LocalDateTime.now());

            if (payload.get("administradorId") != null) {
                Integer adminId = Integer.valueOf(payload.get("administradorId").toString());
                Administrador adminReferencia = new Administrador();
                adminReferencia.setIdUsuario(adminId); 
                nuevoLog.setAdministrador(adminReferencia);
            }

            LogAuditoria guardado = logAuditoriaRepository.save(nuevoLog);
            return ResponseEntity.ok(guardado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar la auditoría: " + e.getMessage());
        }
    }
}