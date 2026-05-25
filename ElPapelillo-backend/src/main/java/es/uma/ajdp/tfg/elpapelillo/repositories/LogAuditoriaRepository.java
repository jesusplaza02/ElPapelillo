package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.LogAuditoria;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Integer> {
    
    
    @Query(value = "SELECT l.* FROM logauditoria l " +
               "JOIN administrador a ON l.administrador_id = a.idUsuario " +
               "WHERE a.id_organizacion = (SELECT id_organizacion FROM administrador WHERE idUsuario = :idUsuario) " +
               "ORDER BY l.fecha DESC", nativeQuery = true)
    List<LogAuditoria> findByOrganizacionDeUsuario(@Param("idUsuario") Integer idUsuario);
}