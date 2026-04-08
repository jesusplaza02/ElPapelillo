package es.uma.ajdp.tfg.elpapelillo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.uma.ajdp.tfg.elpapelillo.models.Representante;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepresentanteRepository extends JpaRepository<Representante, Long> {
    
    // Buscar por DNI para la ficha del representante (C27) 
    Optional<Representante> findByDNI(String dni);

    Optional<Representante> findByemail(String dni);

    
    // Listado de representantes activos para evitar "huérfanos" (C29) 
    List<Representante> findByActivoTrue();
    
    // Buscador por nombre o DNI (C27) 
    List<Representante> findByNombreContainingIgnoreCaseOrDNIContainingIgnoreCase(String nombre, String dni);
}