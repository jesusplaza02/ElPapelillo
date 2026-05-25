package es.uma.ajdp.tfg.elpapelillo.services;

import es.uma.ajdp.tfg.elpapelillo.models.Administrador;
import es.uma.ajdp.tfg.elpapelillo.models.Concurso;
import es.uma.ajdp.tfg.elpapelillo.models.Usuario;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoConcurso;
import es.uma.ajdp.tfg.elpapelillo.repositories.ConcursoRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;

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

        if ("SYSADMIN".equals(usuario.getRol().toUpperCase())) {
            return concursoRepository.findByOrganizacionOpcional(null);
        }

        if (usuario instanceof Administrador) {
            Integer idOrg = ((Administrador) usuario).getId_organizacion();
            return concursoRepository.findByOrganizacionOpcional(idOrg);
        }

        return new ArrayList<>(); 
    }

    public List<Concurso> findActivos() {
        return concursoRepository.findAll(); 
    }

    public Concurso guardar(Concurso concurso) {
        if (concurso.getIdConcurso() != null) {
            return concursoRepository.findById(concurso.getIdConcurso())
                .map(existente -> {
                    existente.setNombre(concurso.getNombre());
                    existente.setEstadoConcurso(concurso.getEstadoConcurso());
                    existente.setFechaInicio(concurso.getFechaInicio());
                    existente.setFechaFin(concurso.getFechaFin());
                    existente.setFechaInicioInscripcion(concurso.getFechaInicioInscripcion());
                    existente.setFechaFinInscripcion(concurso.getFechaFinInscripcion());
                    if (concurso.getId_organizacion() != null) {
                        existente.setId_organizacion(concurso.getId_organizacion());
                    }

                    validarLogicaFechas(existente);
                    return concursoRepository.save(existente);
                }).orElseThrow(() -> new RuntimeException("Concurso no encontrado"));
        } 
        
        validarLogicaFechas(concurso);
        return concursoRepository.save(concurso);
    }

    private void validarLogicaFechas(Concurso c) {
            if (c.getFechaFinInscripcion().isAfter(c.getFechaInicio())) {
                throw new RuntimeException("La inscripción debe terminar antes del inicio del concurso");
            }
            if (c.getFechaInicio().isAfter(c.getFechaFin())) {
                throw new RuntimeException("El concurso no puede terminar antes de empezar");
            }
    }

    @Transactional
    public void eliminarConcurso(Integer id) {
        Concurso concurso = concursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El concurso no existe."));

        if (concurso.getEstadoConcurso() != EstadoConcurso.ACTIVO) {
            throw new RuntimeException("No se puede eliminar un concurso que no esté ACTIVO.");
        }

        if (concurso.getInscripciones() != null && !concurso.getInscripciones().isEmpty()) {
            throw new RuntimeException("No se puede eliminar: El concurso ya tiene agrupaciones asociadas.");
        }

        concursoRepository.deleteById(id);
    }
}