package es.uma.ajdp.tfg.elpapelillo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.uma.ajdp.tfg.elpapelillo.models.Representante;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepresentanteRepository extends JpaRepository<Representante, Integer> {
    
    Optional<Representante> findByDNI(String dni);

    Optional<Representante> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Representante> findByActivoTrue();

    List<Representante> findByNombreContainingIgnoreCaseOrDNIContainingIgnoreCase(String nombre, String dni);
}