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
// Esto es VITAL para que Angular (puerto 4200) pueda leer los datos (puerto 8080)
@CrossOrigin(origins = "http://localhost:4200") 
public class AuditoriaController {

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @GetMapping
    public List<LogAuditoria> obtenerTodosLosLogs() {
        return logAuditoriaRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha"));
    }

    /**
     * Devuelve los logs filtrados por la organización del usuario (Usado por ADMIN/SUPERADMIN)
     * URL: /api/auditoria/usuario?idUsuario=5
     */
    @GetMapping("/usuario")
    public List<LogAuditoria> obtenerLogsPorOrganizacion(@RequestParam("idUsuario") Integer idUsuario) {
        // Llamamos al método personalizado del repositorio
        return logAuditoriaRepository.findByOrganizacionDeUsuario(idUsuario);
    }

 @PostMapping
    public ResponseEntity<?> crearLogAuditoria(@RequestBody Map<String, Object> payload) {
        try {
            System.out.println("====== [AUDITORÍA] PROCESANDO INSERCIÓN DIRECTA ======");
            System.out.println("Payload recibido desde Angular: " + payload);

            LogAuditoria nuevoLog = new LogAuditoria();
            nuevoLog.setAccion((String) payload.get("accion"));
            nuevoLog.setDescripcion((String) payload.get("descripcion"));
            nuevoLog.setFecha(java.time.LocalDateTime.now()); // Hora del servidor

            // Capturamos el ID directamente sin pasar por Repositorios ni hacer SELECT
            if (payload.get("administradorId") != null) {
                Integer adminId = Integer.valueOf(payload.get("administradorId").toString());
                
                // Creamos una instancia "cascarón" de la entidad
                Administrador adminReferencia = new Administrador();
                
                // Le inyectamos el idUsuario que viene del localStorage
                adminReferencia.setIdUsuario(adminId); 
                
                // Se lo asignamos al log. JPA usará este número para rellenar la columna 'administrador_id'
                nuevoLog.setAdministrador(adminReferencia);
                
                System.out.println("[AUDITORÍA] Asignando directamente el ID de usuario al Log: " + adminId);
            } else {
                System.out.println("[AUDITORÍA] ADVERTENCIA: No se recibió 'administradorId', se guardará como NULL.");
            }

            // Se ejecuta un único comando INSERT en tu MySQL
            LogAuditoria guardado = logAuditoriaRepository.save(nuevoLog);
            System.out.println("[AUDITORÍA] ¡Guardado! Nuevo registro ID: " + guardado.getId());
            System.out.println("======================================================");
            
            return ResponseEntity.ok(guardado);

        } catch (Exception e) {
            System.out.println("[AUDITORÍA] ERROR CRÍTICO al insertar directamente:");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar la auditoría: " + e.getMessage());
        }
    }
}