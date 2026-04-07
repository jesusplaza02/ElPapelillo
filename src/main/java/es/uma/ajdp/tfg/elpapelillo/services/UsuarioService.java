package es.uma.ajdp.tfg.elpapelillo.services;

import es.uma.ajdp.tfg.elpapelillo.models.Usuario;
import es.uma.ajdp.tfg.elpapelillo.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de usuarios del sistema.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final DniValidatorService dniValidatorService;
    private final PasswordEncoder passwordEncoder;
    private final LogSistemaService logSistemaService;

    /**
     * Registra un nuevo usuario en el sistema tras validar el DNI.
     *
     * @param usuario datos del nuevo usuario
     * @return usuario guardado
     * @throws IllegalArgumentException si el DNI no es válido o ya está registrado
     */
    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        if (!dniValidatorService.validar(usuario.getDni())) {
            throw new IllegalArgumentException("DNI no válido: " + usuario.getDni());
        }
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            throw new IllegalArgumentException("Ya existe un usuario con el DNI: " + usuario.getDni());
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + usuario.getEmail());
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        Usuario guardado = usuarioRepository.save(usuario);
        logSistemaService.registrarInfo(
                "REGISTRO_USUARIO",
                "Nuevo usuario registrado con DNI: " + usuario.getDni(),
                guardado
        );
        return guardado;
    }

    /**
     * Obtiene todos los usuarios del sistema.
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca un usuario por su ID.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca un usuario por su DNI.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorDni(String dni) {
        return usuarioRepository.findByDni(dni);
    }

    /**
     * Desactiva un usuario del sistema (borrado lógico).
     *
     * @param id identificador del usuario
     * @throws IllegalArgumentException si el usuario no existe
     */
    @Transactional
    public void desactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        logSistemaService.registrarWarning(
                "DESACTIVAR_USUARIO",
                "Usuario desactivado con id: " + id,
                usuario
        );
    }
}
