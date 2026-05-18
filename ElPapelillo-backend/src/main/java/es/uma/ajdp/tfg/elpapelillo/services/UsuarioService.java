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

    @Autowired
    private jakarta.persistence.EntityManager entityManager;



    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Usuario registrarUsuario(Usuario usuario, Integer idEjecutor) throws Exception {
        // 1. Validaciones de DNI y Email
        if (!validarDNI(usuario.getDNI())) {
            throw new Exception("El DNI introducido no es válido.");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new Exception("El correo electrónico ya está registrado.");
        }

        // 2. GENERAR CONTRASEÑA ALEATORIA SEGURA (Texto plano)
        CharacterRule letras = new CharacterRule(EnglishCharacterData.UpperCase, 2);
        CharacterRule digitos = new CharacterRule(EnglishCharacterData.Digit, 2);
        CharacterRule minusculas = new CharacterRule(EnglishCharacterData.LowerCase, 1);
        PasswordGenerator gen = new PasswordGenerator();
        
        String passwordPlana = gen.generatePassword(10, letras, digitos, minusculas);

        // 3. CIFRAR CONTRASEÑA
        String passwordCifrada = passwordEncoder.encode(passwordPlana);
        usuario.setActivo(true);

        // Variable para guardar el resultado final
        Usuario usuarioGuardado;
        String rolUsuario = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";

        // 4. GUARDADO SEGÚN EL ROL (POLIMORFISMO DE JPA)
        if ("SYSADMIN".equals(rolUsuario) || "ADMINISTRADOR".equals(rolUsuario)) {
            // Creamos la entidad Hija directamente
            Administrador nuevoAdmin = new Administrador();
            
            // Copiamos los datos que venían del Front al nuevo objeto Administrador
            nuevoAdmin.setNombre(usuario.getNombre());
            nuevoAdmin.setEmail(usuario.getEmail());
            nuevoAdmin.setPassword(passwordCifrada);
            nuevoAdmin.setDNI(usuario.getDNI());
            nuevoAdmin.setTelefono(usuario.getTelefono());
            nuevoAdmin.setDireccion(usuario.getDireccion());
            nuevoAdmin.setRol(usuario.getRol());
            nuevoAdmin.setActivo(true);
            
            // Asignamos las propiedades específicas de la tabla Administrador
            if ("SYSADMIN".equals(rolUsuario)) {
                nuevoAdmin.setCargo("SYSADMIN");
                nuevoAdmin.setOrganizacion(null);
            } else {
                nuevoAdmin.setCargo("ADMINISTRADOR");
                // nuevoAdmin.setOrganizacion(...);
            }

            // ¡MAGIA DE JPA! Al guardar en el repositorio de Administrador,
            // Hibernate inserta automáticamente en 'usuario', genera el ID, 
            // y luego inserta en 'administrador' usando ese ID. Todo solo y sin fallar.
            usuarioGuardado = administradorRepository.save(nuevoAdmin);
            log.info("[HERENCIA] Guardado Sysadmin/Admin completo mediante JPA.");
            
        } else {
            // Si es un Representante o cualquier otro rol base, se guarda normal
            usuario.setPassword(passwordCifrada);
            usuarioGuardado = usuarioRepository.save(usuario);
            log.info("[BASE] Guardado usuario estándar completo.");
        }

        // 5. ENVIAR EMAIL CON LA CLAVE PLANA
        emailService.enviarEmailInstrucciones(usuarioGuardado.getEmail(), passwordPlana);

        // 6. Auditoría de la acción
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
            .orElseThrow(() -> new Exception("Error: El ejecutor no existe."));
    Usuario objetivo = usuarioRepository.findById(idABorrar)
            .orElseThrow(() -> new Exception("Error: El usuario a borrar no existe."));

    // --- RESTRICCIÓN 1: ANTISUICIDIO (BLOQUEO TOTAL) ---
    // Da igual el rol que seas, no puedes borrarte a ti mismo.
   System.out.println("DEBUG: Rol del objetivo -> [" + objetivo.getRol() + "]");
    if (idABorrar.intValue()==(idEjecutor).intValue()) {
        throw new Exception("Operación cancelada: Un usuario no puede desactivar su propia cuenta.");
    }

    // --- RESTRICCIÓN 2: MÍNIMO 1 SYSADMIN ---
    if ("SYSADMIN".equalsIgnoreCase(objetivo.getRol())) {
        // Buscamos todos los usuarios que tengan ese rol (sin importar mayúsculas) y estén activos
        List<Usuario> sysadminsActivos = usuarioRepository.findByRolIgnoreCaseAndActivoTrue("SYSADMIN");
        
        // Si solo hay uno en la lista, y ese uno es precisamente el que queremos borrar... ¡BLOQUEO!
        if (sysadminsActivos.size() <= 1) {
            throw new Exception("Error de seguridad: No se puede desactivar al último SYSADMIN. El sistema quedaría sin administrador.");
        }
    }

    // --- EL RESTO DE TU LÓGICA (JERARQUÍA Y REPRESENTANTES) ---
    if ("ADMINISTRADOR".equals(objetivo.getRol()) && !"SUPERADMIN".equals(ejecutor.getRol())) {
        throw new Exception("Permiso denegado: Solo el SuperAdmin gestiona Administradores.");
    }

    if ("REPRESENTANTE".equals(objetivo.getRol())) {
        List<Agrupacion> agrupaciones = agrupacionRepository.findByRepresentante_IdUsuario(idABorrar);
        for (Agrupacion ag : agrupaciones) {
            ag.setRepresentante(null);
        }
        agrupacionRepository.saveAll(agrupaciones);
    }

    // EJECUCIÓN
    objetivo.setActivo(false);
    usuarioRepository.save(objetivo);
    registrarLog(ejecutor.getEmail(), "BORRADO_LOGICO", "Desactivado: " + objetivo.getEmail());
}// --- MÉTODOS AUXILIARES ---

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

    public void recuperarPasswordDefinitiva(String email) throws Exception {
        // 1. Buscamos al usuario (usando el orElse(null) para que no te de error de tipos)
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        if (usuario == null) {
            throw new Exception("El usuario no existe");
        }

        // 2. Generamos la nueva clave con las reglas que ya usas en registrarUsuario
        CharacterRule letras = new CharacterRule(EnglishCharacterData.UpperCase, 2);
        CharacterRule digitos = new CharacterRule(EnglishCharacterData.Digit, 2);
        CharacterRule minusculas = new CharacterRule(EnglishCharacterData.LowerCase, 1);
        PasswordGenerator gen = new PasswordGenerator();
        
        String nuevaPasswordPlana = gen.generatePassword(10, letras, digitos, minusculas);

        // 3. Ciframos y guardamos en BDD
        usuario.setPassword(passwordEncoder.encode(nuevaPasswordPlana));
        usuarioRepository.save(usuario);

        // 4. REUTILIZAMOS tu método de email (el que ya tienes funcionando)
        emailService.enviarEmailInstrucciones(usuario.getEmail(), nuevaPasswordPlana);
        
        log.info("Password recuperada para: {}", email);
    }
}