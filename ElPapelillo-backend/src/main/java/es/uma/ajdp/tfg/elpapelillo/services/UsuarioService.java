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
import es.uma.ajdp.tfg.elpapelillo.util.CryptoUtil;
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



    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Usuario registrarUsuario(Usuario usuario, Integer idEjecutor) throws Exception {
        if (!validarDNI(usuario.getDNI())) {
            throw new Exception("El DNI introducido no es válido.");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new Exception("El correo electrónico ya está registrado.");
        }

        CharacterRule letras = new CharacterRule(EnglishCharacterData.UpperCase, 2);
        CharacterRule digitos = new CharacterRule(EnglishCharacterData.Digit, 2);
        CharacterRule minusculas = new CharacterRule(EnglishCharacterData.LowerCase, 1);
        PasswordGenerator gen = new PasswordGenerator();
        
        String passwordPlana = gen.generatePassword(10, letras, digitos, minusculas);

        String passwordCifrada = passwordEncoder.encode(passwordPlana);
        usuario.setActivo(true);

        Usuario usuarioGuardado;
        String rolUsuario = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";

        if ("SYSADMIN".equals(rolUsuario) || "ADMINISTRADOR".equals(rolUsuario)) {
            Administrador nuevoAdmin = new Administrador();

            nuevoAdmin.setNombre(usuario.getNombre());
            nuevoAdmin.setEmail(usuario.getEmail());
            nuevoAdmin.setPassword(passwordCifrada);
            nuevoAdmin.setDNI(usuario.getDNI());
            nuevoAdmin.setTelefono(usuario.getTelefono());
            nuevoAdmin.setDireccion(usuario.getDireccion());
            nuevoAdmin.setRol(usuario.getRol());
            nuevoAdmin.setActivo(true);

            if ("SYSADMIN".equals(rolUsuario)) {
                nuevoAdmin.setCargo("SYSADMIN");
                nuevoAdmin.setOrganizacion(null);
            } else {
                nuevoAdmin.setCargo("ADMINISTRADOR");
            }

            usuarioGuardado = administradorRepository.save(nuevoAdmin);
            log.info("[HERENCIA] Guardado Sysadmin/Admin completo mediante JPA.");
            
        } else {
            usuario.setPassword(passwordCifrada);
            usuarioGuardado = usuarioRepository.save(usuario);
            log.info("[BASE] Guardado usuario estándar completo.");
        }
        emailService.enviarEmailInstrucciones(usuarioGuardado.getEmail(), passwordPlana);

        Usuario ejecutor = usuarioRepository.findById(idEjecutor)
                .orElseThrow(() -> new Exception("Ejecutor no válido"));
        registrarLog(ejecutor.getEmail(), "CREAR_USUARIO", "Usuario creado con éxito: " + usuarioGuardado.getEmail());

        return usuarioGuardado;
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    public List<Usuario> buscarActivosPorNombre(String nombre) {
        return usuarioRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    public Usuario actualizar(Integer id, Usuario datosNuevos, Integer idEjecutor) throws Exception {
        Usuario objetivo = usuarioRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
        
        Usuario ejecutor = usuarioRepository.findById(idEjecutor)
                .orElseThrow(() -> new Exception("Ejecutor no válido"));

        objetivo.setNombre(datosNuevos.getNombre());
        objetivo.setTelefono(datosNuevos.getTelefono());
        objetivo.setDireccion(datosNuevos.getDireccion());
        objetivo.setEmail(datosNuevos.getEmail());
        objetivo.setDNI(datosNuevos.getDNI());
        
        if (datosNuevos.isActivo() != objetivo.isActivo()) { 
            objetivo.setActivo(datosNuevos.isActivo());
        }

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

        registrarLog(ejecutor.getEmail(), "ACTUALIZAR_USUARIO", "Se actualizaron los datos de: " + actualizado.getEmail());
        
        return actualizado;
    }

    public void eliminarUsuarioLogico(Integer idABorrar, Integer idEjecutor) throws Exception {
        Usuario ejecutor = usuarioRepository.findById(idEjecutor)
            .orElseThrow(() -> new Exception("Error: El ejecutor no existe."));
    Usuario objetivo = usuarioRepository.findById(idABorrar)
            .orElseThrow(() -> new Exception("Error: El usuario a borrar no existe."));

   System.out.println("DEBUG: Rol del objetivo -> [" + objetivo.getRol() + "]");
    if (idABorrar.intValue()==(idEjecutor).intValue()) {
        throw new Exception("Operación cancelada: Un usuario no puede desactivar su propia cuenta.");
    }

    if ("SYSADMIN".equalsIgnoreCase(objetivo.getRol())) {
        List<Usuario> sysadminsActivos = usuarioRepository.findByRolIgnoreCaseAndActivoTrue("SYSADMIN");
        
        if (sysadminsActivos.size() <= 1) {
            throw new Exception("Error de seguridad: No se puede desactivar al último SYSADMIN. El sistema quedaría sin administrador.");
        }
    }

    if ("ADMINISTRADOR".equals(objetivo.getRol()) && !"SUPERADMIN".equals(ejecutor.getRol())) {
        throw new Exception("Permiso denegado: Solo el SuperAdmin gestiona Administradores.");
    }

    objetivo.setActivo(false);
    usuarioRepository.save(objetivo);
    registrarLog(ejecutor.getEmail(), "BORRADO_LOGICO", "Desactivado: " + objetivo.getEmail());
}

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

   public void recuperarPasswordDefinitiva(String emailPlano) throws Exception {
        if (emailPlano == null || emailPlano.trim().isEmpty()) {
            throw new Exception("El correo electrónico no puede estar vacío");
        }

        String emailCifrado = CryptoUtil.encrypt(emailPlano.trim().toLowerCase());

        Usuario usuario = usuarioRepository.findByEmail(emailCifrado).orElse(null);
        
        if (usuario == null) {
            throw new Exception("El usuario no existe en la base de datos.");
        }

        CharacterRule letras = new CharacterRule(EnglishCharacterData.UpperCase, 2);
        CharacterRule digitos = new CharacterRule(EnglishCharacterData.Digit, 2);
        CharacterRule minusculas = new CharacterRule(EnglishCharacterData.LowerCase, 1);
        PasswordGenerator gen = new PasswordGenerator();
        
        String nuevaPasswordPlana = gen.generatePassword(10, letras, digitos, minusculas);

        usuario.setPassword(passwordEncoder.encode(nuevaPasswordPlana));
        usuarioRepository.save(usuario);

        emailService.enviarEmailInstrucciones(emailPlano, nuevaPasswordPlana);
        
        log.info("Password recuperada y enviada para el usuario existente.");
    }
}