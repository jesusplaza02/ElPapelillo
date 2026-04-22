package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.LoginRequest;
import es.uma.ajdp.tfg.elpapelillo.models.LoginResponse;
import es.uma.ajdp.tfg.elpapelillo.models.Usuario;
import es.uma.ajdp.tfg.elpapelillo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // Inyectamos el encriptador

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginData) {
        try {
            // 1. BUSQUEDA: Buscamos al usuario por el email que viene de Angular
            Optional<Usuario> userOpt = usuarioRepository.findByEmail(loginData.getEmail());

            if (userOpt.isPresent()) {
                Usuario user = userOpt.get();

                // 2. VERIFICACIÓN CON BCRYPT: 
                // Comparamos la contraseña plana de Angular con el hash de la BD
                if (passwordEncoder.matches(loginData.getPassword(), user.getPassword())) {
                    
                    // Si todo es correcto, enviamos los datos REALES
                    LoginResponse res = new LoginResponse(
                        "token-generado-abc", // JWT simplificado
                        user.getRol(),        
                        user.getEmail(),      
                        user.getIdUsuario()   
                    );
                    
                    return ResponseEntity.ok(res);
                }
            }

            // 3. ERROR: Si no existe o la clave no coincide
            // Usamos un Map para que Angular reciba un JSON con el mensaje
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("message", "Email o contraseña incorrectos"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("message", "Error en el servidor: " + e.getMessage()));
        }
    }
}