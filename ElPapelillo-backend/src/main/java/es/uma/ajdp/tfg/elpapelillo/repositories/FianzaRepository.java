package es.uma.ajdp.tfg.elpapelillo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.uma.ajdp.tfg.elpapelillo.models.Fianza;

@Repository
public interface FianzaRepository extends JpaRepository<Fianza, Integer> {
}