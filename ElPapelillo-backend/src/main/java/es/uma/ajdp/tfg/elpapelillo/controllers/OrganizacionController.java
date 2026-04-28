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

    @PutMapping("/{id}")
    public ResponseEntity<Organizacion> actualizar(@PathVariable @NonNull Integer id, @RequestBody @NonNull Organizacion orgActualizada) {
        return repository.findById(id).map(org -> {
            // Actualizamos los campos con lo que viene de Angular
            org.setNombre(orgActualizada.getNombre());
            org.setEmail(orgActualizada.getEmail());
            org.setTelefono(orgActualizada.getTelefono());
            org.setUbicacion(orgActualizada.getUbicacion());
            
            // Crucial: aquí se recibe el 'activo: false' del borrado lógico
            org.setActivo(orgActualizada.getActivo()); 

            Organizacion guardada = repository.save(org);
            return ResponseEntity.ok(guardada);
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @NonNull Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}