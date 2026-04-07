package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.LogSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad LogSistema.
 */
@Repository
public interface LogSistemaRepository extends JpaRepository<LogSistema, Long> {

    List<LogSistema> findByUsuarioId(Long usuarioId);

    List<LogSistema> findByNivel(LogSistema.NivelLog nivel);

    List<LogSistema> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);

    List<LogSistema> findTop50ByOrderByFechaHoraDesc();
}
