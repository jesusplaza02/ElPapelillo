package es.uma.ajdp.tfg.elpapelillo.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import es.uma.ajdp.tfg.elpapelillo.models.LogAuditoria;
import es.uma.ajdp.tfg.elpapelillo.repositories.LogAuditoriaRepository;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
// Esto es VITAL para que Angular (puerto 4200) pueda leer los datos (puerto 8080)
@CrossOrigin(origins = "http://localhost:4200") 
public class AuditoriaController {

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

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
}