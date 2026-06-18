package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.Representante;
import es.uma.ajdp.tfg.elpapelillo.repositories.RepresentanteRepository;
import es.uma.ajdp.tfg.elpapelillo.services.EmailService; // 🌟 IMPORTANTE: Importamos tu EmailService
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200") 
public class RegistroController {

    @Autowired
    private RepresentanteRepository representanteRepository;

    // 🌟 Cambiamos JavaMailSender por tu EmailService blindado
    @Autowired
    private EmailService emailService; 

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/registro")
    @Transactional
    public ResponseEntity<?> registrarUsuario(@RequestBody Map<String, Object> payload) {
        try {
            String email = (String) payload.get("email");
            String nombre = (String) payload.get("nombre");
            String apellidos = (String) payload.get("apellidos");
            String dni = (String) payload.get("dni");
            String direccion = (String) payload.get("direccion");
            String telefono = (String) payload.get("telefono");
            String telEmergencia = (String) payload.get("telEmergencia");

            if (email == null || email.isBlank() || nombre == null || nombre.isBlank() || 
                apellidos == null || apellidos.isBlank() || dni == null || dni.isBlank() ||
                direccion == null || direccion.isBlank() || telefono == null || telefono.isBlank() ||
                telEmergencia == null || telEmergencia.isBlank()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Todos los campos del formulario son obligatorios."));
            }

            if (!validarDNI(dni)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "El DNI introducido no es válido (formato o letra incorrecta)."));
            }

            if (representanteRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "El correo electrónico ya está registrado en el sistema."));
            }

            String passwordPlana = UUID.randomUUID().toString().substring(0, 8);
            String passwordEncriptada = passwordEncoder.encode(passwordPlana);
            
            Representante rep = new Representante();
            rep.setNombre(nombre + " " + apellidos);
            rep.setEmail(email);
            rep.setDNI(dni);
            rep.setRol("REPRESENTANTE");
            rep.setDireccion(direccion);
            rep.setActivo(true);
            rep.setPassword(passwordEncriptada);
            rep.setTelefono(telefono);
            rep.setContacto_emergencia(telEmergencia);

            representanteRepository.save(rep);

            // 🌟 SOLUCIÓN: Usamos la variable local 'email' que viene limpia del JSON
            // en lugar de usar rep.getEmail() que mutó por el @PrePersist.
            // Además, llamamos a tu EmailService estructurado.
            emailService.enviarEmailInstrucciones(email, passwordPlana);

            return ResponseEntity.ok(Map.of("status", "ok", "message", "Registro completado. Revisa tu email."));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("message", "Error interno en el servidor: " + e.getMessage()));
        }
    }

    private boolean validarDNI(String dni) {
        if (dni == null || !dni.matches("^[0-9]{8}[A-Z]$")) return false;
        try {
            String numeros = dni.substring(0, 8);
            char letraEntrada = dni.charAt(8);
            String letrasValidas = "TRWAGMYFPDXBNJZSQVHLCKE";
            int indice = Integer.parseInt(numeros) % 23;
            return letrasValidas.charAt(indice) == letraEntrada;
        } catch (Exception e) {
            return false;
        }
    }
    
    // 🌟 He eliminado el método manual "enviarEmail" que tenías aquí abajo 
    // porque duplicaba lógica y rompía el descifrado del TFG.
}