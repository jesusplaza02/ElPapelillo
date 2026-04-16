package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.Documento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;


@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByAgrupacionIdAgrupacion(Integer idAgrupacion);
    List<Documento> findByEstado(EstadoAdministrativo estado);
}