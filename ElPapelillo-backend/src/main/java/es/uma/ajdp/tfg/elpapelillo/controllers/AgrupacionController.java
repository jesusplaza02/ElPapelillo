package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.Agrupacion;
import es.uma.ajdp.tfg.elpapelillo.services.AgrupacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agrupaciones")
@CrossOrigin(origins = "http://localhost:4200") // Permite que Angular se conecte
public class AgrupacionController {

    @Autowired
    private AgrupacionService agrupacionService;

    @GetMapping("/representante/{id}")
    public List<Agrupacion> getAgrupaciones(@PathVariable Integer id) {
        return agrupacionService.findByRepresentanteId(id);
    }
}