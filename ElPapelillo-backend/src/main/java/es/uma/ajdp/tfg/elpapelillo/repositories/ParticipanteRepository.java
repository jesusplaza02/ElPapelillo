package es.uma.ajdp.tfg.elpapelillo.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.uma.ajdp.tfg.elpapelillo.models.Participante;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Integer> {
    // Busca un participante exacto por su DNI
    Optional<Participante> findByDni(String dni);
}