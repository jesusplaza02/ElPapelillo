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

    /**
     * Devuelve todos los logs de auditoría ordenados por fecha descendente
     * (Lo más nuevo primero para que se vea arriba en el panel)
     */
    @GetMapping
    public List<LogAuditoria> obtenerLogs() {
        return logAuditoriaRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha"));
    }
}