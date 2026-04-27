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

import org.springframework.beans.factory.annotation.Autowired;
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

    // private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // REGISTRAR USUARIO 
    public Usuario registrarUsuario(Usuario usuario) throws Exception {
        if (!validarDNI(usuario.getDNI())) {
            throw new Exception("El DNI introducido no es válido (algoritmo incorrecto).");
        }

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new Exception("El correo electrónico ya está registrado.");
        }

        // Asignamos el DNI como contraseña provisional si no viene ninguna
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            usuario.setPassword(usuario.getDNI());
        }

        usuario.setActivo(true);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        emailService.enviarEmailInstrucciones(usuarioGuardado.getEmail());
        
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