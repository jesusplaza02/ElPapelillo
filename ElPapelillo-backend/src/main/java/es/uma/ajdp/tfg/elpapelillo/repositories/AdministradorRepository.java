package es.uma.ajdp.tfg.elpapelillo.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.uma.ajdp.tfg.elpapelillo.models.Administrador;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {
    
    // Para el listado de usuarios administradores (C23) 
    // Filtra solo los que no han sido eliminados lógicamente (C25) 
    List<Administrador> findByActivoTrue();

    Administrador findByEmail(String email);
    Optional<Administrador> findByIdUsuario(Integer idUsuario);
    Administrador findByDNI(String dni);
    Administrador findByCargo(String cargo);
    
    // Buscar por nombre o apellidos para el buscador (C23)
    List<Administrador> findByNombreContainingIgnoreCase(String nombre);
}
