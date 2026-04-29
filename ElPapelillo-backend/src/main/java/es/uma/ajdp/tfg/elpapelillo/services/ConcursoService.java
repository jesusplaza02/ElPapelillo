package es.uma.ajdp.tfg.elpapelillo.services;

import es.uma.ajdp.tfg.elpapelillo.models.Administrador;
import es.uma.ajdp.tfg.elpapelillo.models.Concurso;
import es.uma.ajdp.tfg.elpapelillo.models.Usuario;
import es.uma.ajdp.tfg.elpapelillo.repositories.ConcursoRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConcursoService {

    @Autowired
    private ConcursoRepository concursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Optional<Concurso> findById(Integer id) {
        return concursoRepository.findById(id);
    }

    public List<Concurso> listarConcursosSegunRol(Integer idUsuarioActual) {
    Usuario usuario = usuarioRepository.findById(idUsuarioActual)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    String rol = usuario.getRol().toUpperCase();

    // 1. Si es SYSADMIN, pasamos null para que el Repository lo traiga TODO
    if ("SYSADMIN".equals(usuario.getRol().toUpperCase())) {
        return concursoRepository.findByOrganizacionOpcional(null);
    }

    // 2. Si es ADMIN o SUPERADMIN, buscamos su ID de organización
    if (usuario instanceof Administrador) {
        Integer idOrg = ((Administrador) usuario).getId_organizacion();
        return concursoRepository.findByOrganizacionOpcional(idOrg);
    }

    // 3. Otros roles (como Representante) podrían ver solo los suyos o nada
    return new ArrayList<>(); 
}

    public List<Concurso> findActivos() {
        // Opción A: Si tienes columna estado en la BD
        //return concursoRepository.findByEstadoConcurso(EstadoConcurso.ACTIVO);
        
        // Opción B: Si aún no tienes estados y quieres probar que el select cargue algo:
        return concursoRepository.findAll(); 
    }
}