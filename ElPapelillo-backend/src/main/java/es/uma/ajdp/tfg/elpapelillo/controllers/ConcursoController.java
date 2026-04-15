package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.Concurso;
import es.uma.ajdp.tfg.elpapelillo.services.ConcursoService; // Asumiendo que tienes este service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/concursos") // <--- Esto coincide con tu error 404
@CrossOrigin(origins = "http://localhost:4200")
public class ConcursoController {

    @Autowired
    private ConcursoService concursoService;

    @GetMapping("/activos") // <--- Esto completa la URL: /api/concursos/activos
    public List<Concurso> getActivos() {
        return concursoService.findActivos();
    }
}