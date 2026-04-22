package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.LoginRequest;
import es.uma.ajdp.tfg.elpapelillo.models.LoginResponse;
import es.uma.ajdp.tfg.elpapelillo.models.Usuario;
import es.uma.ajdp.tfg.elpapelillo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository; // La conexión a la BD

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginData) {
        
        // 1. BUSQUEDA DINÁMICA: Buscamos al usuario por el email que viene de Angular
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(loginData.getEmail());

        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();

            // 2. VERIFICACIÓN: Comparamos la contraseña 
            // (Nota: Si usas BCrypt en el futuro, aquí se usa passwordEncoder.matches)
            if (user.getPassword().equals(loginData.getPassword())) {
                
                // Si todo es correcto, enviamos los datos REALES de la base de datos
                LoginResponse res = new LoginResponse(
                    "token-generado-abc", // Aquí iría el JWT real más adelante
                    user.getRol(),        // El rol de tu tabla (Administrador/Representante)
                    user.getEmail(),      
                    user.getIdUsuario()           // El ID real del usuario
                );
                
                return ResponseEntity.ok(res);
            }
        }

        // 3. ERROR: Si el usuario no existe o la contraseña no coincide
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body("Email o contraseña incorrectos");
    }
}