package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.Usuario;
import es.uma.ajdp.tfg.elpapelillo.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<?> crearUsuario(
            @RequestBody Usuario nuevoUsuario,
            @RequestParam Integer idEjecutor) {
        try {
            // CAMBIO: Llamamos a registrarUsuario que es el método que existe en el Service
            Usuario creado = usuarioService.registrarUsuario(nuevoUsuario, idEjecutor); 
            return ResponseEntity.ok(creado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear usuario: " + e.getMessage());
        }
    }

    // LISTAR TODOS LOS USUARIOS
    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        List<Usuario> usuarios = usuarioService.obtenerTodos();
        return ResponseEntity.ok(usuarios);
    }

    // BUSCAR UN USUARIO ESPECÍFICO POR ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Integer id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar-activos/{nombre}")
    @CrossOrigin(origins = "http://localhost:4200")
        public ResponseEntity<List<Usuario>> buscarActivosPorNombre(@PathVariable String nombre) {
        List<Usuario> usuarios = usuarioService.buscarActivosPorNombre(nombre);
    
        // Si no hay resultados, devolvemos una lista vacía con estatus 200
        // Esto evita errores de parseo en el Frontend
        return ResponseEntity.ok(usuarios);
    }

    // ACCIÓN DE ADMIN: BORRADO LÓGICO (DESACTIVAR)
    // El idAdmin se usa para saber quién ejecutó la acción y guardarlo en el log
    @DeleteMapping("/{id}/desactivar")
    public ResponseEntity<String> desactivarUsuario(
            @PathVariable Integer id, 
            @RequestParam Integer idAdmin) {
        try {
            usuarioService.eliminarUsuarioLogico(id, idAdmin);
            return ResponseEntity.ok("Usuario desactivado con éxito. La acción ha sido registrada.");
        } catch (Exception e) {
            // Si el idAdmin no tiene permisos, el service lanzará una excepción
            return ResponseEntity.badRequest().body("Error de seguridad: " + e.getMessage());
        }
    }

    // ACTUALIZAR DATOS DE USUARIO
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable Integer id, 
            @RequestBody Usuario datosNuevos,
            @RequestParam Integer idEjecutor) {
        try {
            Usuario actualizado = usuarioService.actualizar(id, datosNuevos, idEjecutor);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("No se pudo actualizar: " + e.getMessage());
        }
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<?> recuperar(@RequestBody Map<String, String> payload) {
        try {
            usuarioService.recuperarPasswordDefinitiva(payload.get("email"));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}