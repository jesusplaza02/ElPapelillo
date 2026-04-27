package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.LoginRequest;
import es.uma.ajdp.tfg.elpapelillo.models.LoginResponse;
import es.uma.ajdp.tfg.elpapelillo.models.Usuario;
import es.uma.ajdp.tfg.elpapelillo.models.Administrador;
import es.uma.ajdp.tfg.elpapelillo.repositories.UsuarioRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.AdministradorRepository;
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
    private AdministradorRepository administradorRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginData) {
        try {
            // 1. Buscamos al usuario por email
            Optional<Usuario> userOpt = usuarioRepository.findByEmail(loginData.getEmail());

            if (userOpt.isPresent()) {
                Usuario user = userOpt.get();

                // 2. Verificamos contraseña con BCrypt
                if (passwordEncoder.matches(loginData.getPassword(), user.getPassword())) {
                    
                    Integer orgId = null;

                    // 3. Obtener el ID de la organización desde la tabla Administrador
                    // Filtramos por roles que pertenecen a organizaciones
                    if ("ADMINISTRADOR".equals(user.getRol()) || "SUPERADMIN".equals(user.getRol())) {
                        
                        // Buscamos en el repo de administradores usando el idUsuario
                        Optional<Administrador> adminOpt = administradorRepository.findByIdUsuario(user.getIdUsuario());
                        
                        if (adminOpt.isPresent()) {
                            Administrador admin = adminOpt.get();
                            
                            // Accedemos al objeto Organizacion y sacamos su ID
                            // (Asegúrate de que en la clase Organizacion el ID se llame 'id')
                            if (admin.getOrganizacion() != null) {
                                orgId = admin.getOrganizacion().getIdOrganizacion(); 
                            }
                        }
                    }

                    // 4. Enviamos la respuesta con los 5 campos requeridos
                    LoginResponse res = new LoginResponse(
                        "token-generado-abc", 
                        user.getRol(),        
                        user.getEmail(),    
                        user.getIdUsuario(),
                        orgId 
                    );
                    
                    return ResponseEntity.ok(res);
                }
            }

            // Error de autenticación
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("message", "Email o contraseña incorrectos"));

        } catch (Exception e) {
            // Error de servidor
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("message", "Error en el servidor: " + e.getMessage()));
        }
    }
}