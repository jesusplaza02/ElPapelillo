package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.Organizacion;
import es.uma.ajdp.tfg.elpapelillo.repositories.OrganizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizaciones")
@CrossOrigin(origins = "http://localhost:4200")
public class OrganizacionController {

    @Autowired
    private OrganizacionRepository repository;

    @GetMapping
    public List<Organizacion> listar() {
        return repository.findAll();
    }

    @PostMapping
    public Organizacion guardar(@RequestBody @NonNull Organizacion org) {
        return repository.save(org);
    }

    /**
     * CORRECCIÓN: Cambiado de Long a Integer para coincidir con OrganizacionRepository
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @NonNull Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}