package es.uma.ajdp.tfg.elpapelillo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.uma.ajdp.tfg.elpapelillo.models.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByRolIgnoreCaseAndActivoTrue(String rol);
   
    boolean existsByEmail(String email);
    boolean existsByDNI(String dni);

    // Para listar solo los usuarios que no han sido borrados 
    List<Usuario> findByActivoTrue();

    List<Usuario> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    long countByRolAndActivoTrue(String rol);

}