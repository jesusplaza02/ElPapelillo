package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.Documento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByAgrupacionIdAgrupacion(Integer idAgrupacion);
}