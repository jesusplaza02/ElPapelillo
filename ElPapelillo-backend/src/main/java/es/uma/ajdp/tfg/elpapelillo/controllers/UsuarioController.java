package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.Usuario;
import es.uma.ajdp.tfg.elpapelillo.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

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
}