package es.uma.ajdp.tfg.elpapelillo.services;

import es.uma.ajdp.tfg.elpapelillo.models.Administrador; 
import es.uma.ajdp.tfg.elpapelillo.models.Representante; 
import es.uma.ajdp.tfg.elpapelillo.models.Agrupacion; // AÑADIDO
import es.uma.ajdp.tfg.elpapelillo.models.LogAuditoria;
import es.uma.ajdp.tfg.elpapelillo.models.Usuario;
import es.uma.ajdp.tfg.elpapelillo.repositories.AdministradorRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.AgrupacionRepository; // AÑADIDO
import es.uma.ajdp.tfg.elpapelillo.repositories.LogAuditoriaRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private AgrupacionRepository agrupacionRepository;

    @Autowired
    private EmailService emailService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();



    public Usuario registrarUsuario(Usuario usuario, Integer idEjecutor) throws Exception {
        // 1. Validaciones de DNI y Email
        if (!validarDNI(usuario.getDNI())) {
            throw new Exception("El DNI introducido no es válido.");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new Exception("El correo electrónico ya está registrado.");
        }

        // 2. GENERAR CONTRASEÑA ALEATORIA SEGURA (Texto plano)
        // Usamos las reglas que tenías: 2 mayúsculas, 2 dígitos, 1 minúscula
        CharacterRule letras = new CharacterRule(EnglishCharacterData.UpperCase, 2);
        CharacterRule digitos = new CharacterRule(EnglishCharacterData.Digit, 2);
        CharacterRule minusculas = new CharacterRule(EnglishCharacterData.LowerCase, 1);
        PasswordGenerator gen = new PasswordGenerator();
        
        String passwordPlana = gen.generatePassword(10, letras, digitos, minusculas);

        // 3. CIFRAR PARA LA BASE DE DATOS ($2a$10$...)
        // IMPORTANTE: Se guarda el hash cifrado, nunca la plana
        usuario.setPassword(passwordEncoder.encode(passwordPlana));
        usuario.setActivo(true);
        
        // 4. GUARDAR EN BD
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 5. ENVIAR EMAIL CON LA CLAVE PLANA
        // Le pasamos la 'passwordPlana' para que el usuario reciba la que puede leer
        emailService.enviarEmailInstrucciones(usuarioGuardado.getEmail(), passwordPlana);

        // 6. Auditoría
        Usuario ejecutor = usuarioRepository.findById(idEjecutor)
                .orElseThrow(() -> new Exception("Ejecutor no válido"));
        registrarLog(ejecutor.getEmail(), "CREAR_USUARIO", "Usuario creado con éxito: " + usuarioGuardado.getEmail());

        return usuarioGuardado;
    }

    // OBTENER TODOS 
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    // BUSCAR POR ID 
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    public List<Usuario> buscarActivosPorNombre(String nombre) {
        return usuarioRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    // ACTUALIZAR 
    public Usuario actualizar(Integer id, Usuario datosNuevos, Integer idEjecutor) throws Exception {
        Usuario objetivo = usuarioRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
        
        Usuario ejecutor = usuarioRepository.findById(idEjecutor)
                .orElseThrow(() -> new Exception("Ejecutor no válido"));

        // 1. Actualizamos todos los campos base
        objetivo.setNombre(datosNuevos.getNombre());
        objetivo.setTelefono(datosNuevos.getTelefono());
        objetivo.setDireccion(datosNuevos.getDireccion());
        objetivo.setEmail(datosNuevos.getEmail());
        objetivo.setDNI(datosNuevos.getDNI());
        
        // ¡ESTO ARREGLA EL BORRADO LÓGICO DESDE EL FRONTEND!
        if (datosNuevos.isActivo() != objetivo.isActivo()) { 
            objetivo.setActivo(datosNuevos.isActivo());
        }

        // 2. Actualizamos campos específicos usando casting seguro
        if ("ADMINISTRADOR".equalsIgnoreCase(objetivo.getRol()) && datosNuevos instanceof Administrador) {
            Administrador adminObj = (Administrador) objetivo;
            Administrador adminNuevos = (Administrador) datosNuevos;
            adminObj.setCargo(adminNuevos.getCargo());
        } 
        else if ("REPRESENTANTE".equalsIgnoreCase(objetivo.getRol()) && datosNuevos instanceof Representante) {
            Representante repObj = (Representante) objetivo;
            Representante repNuevos = (Representante) datosNuevos;
            repObj.setContacto_emergencia(repNuevos.getContacto_emergencia()); 
        }

        Usuario actualizado = usuarioRepository.save(objetivo);

        // Registro de Auditoría
        registrarLog(ejecutor.getEmail(), "ACTUALIZAR_USUARIO", "Se actualizaron los datos de: " + actualizado.getEmail());
        
        return actualizado;
    }

    // BORRADO LÓGICO 
    public void eliminarUsuarioLogico(Integer idABorrar, Integer idEjecutor) throws Exception {
        Usuario ejecutor = usuarioRepository.findById(idEjecutor)
                .orElseThrow(() -> new Exception("Error: El usuario ejecutor no existe."));

        Usuario objetivo = usuarioRepository.findById(idABorrar)
                .orElseThrow(() -> new Exception("Error: El usuario a borrar no existe."));

        if ("ADMIN".equals(objetivo.getRol()) && !"SUPERADMIN".equals(ejecutor.getRol())) {
            throw new Exception("Permiso denegado: Solo SuperAdmin gestiona Admins.");
        }

        // CAMBIO AQUÍ: Ahora buscamos en Agrupaciones
        if ("REPRESENTANTE".equals(objetivo.getRol())) {
            // Buscamos las agrupaciones de este representante
            List<Agrupacion> agrupacionesAsociadas = agrupacionRepository.findByRepresentante_IdUsuario(idABorrar);
            
            if (!agrupacionesAsociadas.isEmpty()) {
                for (Agrupacion agrupacion : agrupacionesAsociadas) {
                    // Desvinculamos al representante de la agrupación
                    agrupacion.setRepresentante(null); 
                }
                agrupacionRepository.saveAll(agrupacionesAsociadas);
                log.info("Se han desvinculado {} agrupaciones del representante {}", 
                         agrupacionesAsociadas.size(), objetivo.getEmail());
            }
        }

        objetivo.setActivo(false);
        usuarioRepository.save(objetivo);
        
        registrarLog(ejecutor.getEmail(), "BORRADO_LOGICO", "Desactivado usuario: " + objetivo.getEmail());
    }
    
    // --- MÉTODOS AUXILIARES ---

    private String generarPasswordAleatoria(int longitud) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom rnd = new java.security.SecureRandom();
        for (int i = 0; i < longitud; i++) {
            sb.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return sb.toString();
    }

    private boolean validarDNI(String dni) {
        if (dni == null || !dni.matches("^[0-9]{8}[A-Z]$")) return false;
        String numeros = dni.substring(0, 8);
        char letraEntrada = dni.charAt(8);
        String letrasValidas = "TRWAGMYFPDXBNJZSQVHLCKE";
        int indice = Integer.parseInt(numeros) % 23;
        return letrasValidas.charAt(indice) == letraEntrada;
    }
    
    private void registrarLog(String email, String accion, String desc) {
        LogAuditoria log = new LogAuditoria();
        log.setAdministrador(administradorRepository.findByEmail(email)); 
        log.setAccion(accion);
        log.setDescripcion(desc);
        logAuditoriaRepository.save(log);
    }
}