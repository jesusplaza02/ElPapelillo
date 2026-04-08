package es.uma.ajdp.tfg.elpapelillo.repositories;

import es.uma.ajdp.tfg.elpapelillo.models.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    // Al extender de JpaRepository, ya están disponibles métodos como save(), findAll(), findById(), delete(), etc.
}