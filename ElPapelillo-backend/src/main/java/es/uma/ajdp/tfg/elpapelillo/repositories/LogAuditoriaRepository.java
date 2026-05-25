package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.LogAuditoria;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {

    @Query("SELECT l FROM LogAuditoria l WHERE l.administrador.idUsuario IN (" +
           "  SELECT u.idUsuario FROM Usuario u WHERE u.organizacion.idOrganizacion = (" +
           "    SELECT req.organizacion.idOrganizacion FROM Usuario req WHERE req.idUsuario = :idUsuario" +
           "  )" +
           ") ORDER BY l.fecha DESC")
    List<LogAuditoria> findByOrganizacionDeUsuario(@Param("idUsuario") Integer idUsuario);
}