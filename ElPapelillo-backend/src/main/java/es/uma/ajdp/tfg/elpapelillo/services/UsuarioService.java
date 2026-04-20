package es.uma.ajdp.tfg.elpapelillo.services;

import es.uma.ajdp.tfg.elpapelillo.models.Agrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.LogAuditoria;
import es.uma.ajdp.tfg.elpapelillo.models.Usuario;
import es.uma.ajdp.tfg.elpapelillo.repositories.AdministradorRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.AgrupacionRepository;
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
    private AgrupacionRepository agrupacionRepository;

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private EmailService emailService;

    // private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // REGISTRAR USUARIO 
    public Usuario registrarUsuario(Usuario usuario) throws Exception {
        if (!validarDNI(usuario.getDNI())) {
            throw new Exception("El DNI introducido no es válido (algoritmo incorrecto).");
        }

        // Importante: verificar que el método en el repo se llame findByEmail o findByCorreo
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new Exception("El correo electrónico ya está registrado.");
        }

        // String passCifrada = passwordEncoder.encode(usuario.getPassword());
        // usuario.setPassword(passCifrada);
        usuario.setPassword("");
        usuario.setActivo(true);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        emailService.enviarEmailInstrucciones(usuarioGuardado.getEmail());
        
        return usuarioGuardado;
    }

    // OBTENER TODOS hacer cargas parciales para mostrar diferentes mostrar diferentes páginas 
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

        // Actualizamos campos básicos
        objetivo.setNombre(datosNuevos.getNombre());
        objetivo.setTelefono(datosNuevos.getTelefono());
        objetivo.setDireccion(datosNuevos.getDireccion());
        
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

        if ("REPRESENTANTE".equals(objetivo.getRol())) {
            // Buscamos todas las agrupaciones asociadas a este representante
            List<Agrupacion> agrupacionesAsociadas = agrupacionRepository.findByRepresentanteId(idABorrar);
            
            if (!agrupacionesAsociadas.isEmpty()) {
                for (Agrupacion agrupacion : agrupacionesAsociadas) {
                    agrupacion.setRepresentante(null); // Dejamos la agrupación "huérfana"
                }
                // Guardamos los cambios en las agrupaciones
                agrupacionRepository.saveAll(agrupacionesAsociadas);
                log.info("Se han dejado huérfanas {} agrupaciones del representante {}", 
                         agrupacionesAsociadas.size(), objetivo.getEmail());
            }
        }

        objetivo.setActivo(false);
        usuarioRepository.save(objetivo);
        
        // Registro de Auditoría
        registrarLog(ejecutor.getEmail(), "BORRADO_LOGICO", "Desactivado usuario: " + objetivo.getEmail());}
    // --- MÉTODOS AUXILIARES ---

    /*public boolean validarCredenciales(String correo, String passwordSinCifrar) {
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(correo);
        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            return passwordEncoder.matches(passwordSinCifrar, user.getPassword()) && user.isActivo();
        }
        return false;
    }*/

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
        log.setAdministrador(administradorRepository.findByEmail(email)); // Asocia el log al admin que hizo la acción
        log.setAccion(accion);
        log.setDescripcion(desc);
        logAuditoriaRepository.save(log);
    }
}