package es.uma.ajdp.tfg.elpapelillo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.uma.ajdp.tfg.elpapelillo.models.Participante;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Integer> {

    Optional<Participante> findByDni(String dni);
    List<Participante> findAllByDni(String dni);
    
}