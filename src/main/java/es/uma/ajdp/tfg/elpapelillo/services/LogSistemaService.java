package es.uma.ajdp.tfg.elpapelillo.services;

import es.uma.ajdp.tfg.elpapelillo.models.LogSistema;
import es.uma.ajdp.tfg.elpapelillo.models.Usuario;
import es.uma.ajdp.tfg.elpapelillo.repositories.LogSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para la gestión de logs del sistema.
 */
@Service
@RequiredArgsConstructor
public class LogSistemaService {

    private static final Logger logger = LoggerFactory.getLogger(LogSistemaService.class);

    private final LogSistemaRepository logSistemaRepository;

    /**
     * Registra una acción informativa en el sistema.
     */
    @Transactional
    public LogSistema registrarInfo(String accion, String descripcion, Usuario usuario) {
        return guardarLog(accion, descripcion, usuario, LogSistema.NivelLog.INFO);
    }

    /**
     * Registra una advertencia en el sistema.
     */
    @Transactional
    public LogSistema registrarWarning(String accion, String descripcion, Usuario usuario) {
        return guardarLog(accion, descripcion, usuario, LogSistema.NivelLog.WARNING);
    }

    /**
     * Registra un error en el sistema.
     */
    @Transactional
    public LogSistema registrarError(String accion, String descripcion, Usuario usuario) {
        return guardarLog(accion, descripcion, usuario, LogSistema.NivelLog.ERROR);
    }

    /**
     * Registra un evento crítico en el sistema.
     */
    @Transactional
    public LogSistema registrarCritico(String accion, String descripcion, Usuario usuario) {
        return guardarLog(accion, descripcion, usuario, LogSistema.NivelLog.CRITICO);
    }

    /**
     * Obtiene los últimos 50 logs del sistema.
     */
    @Transactional(readOnly = true)
    public List<LogSistema> obtenerUltimosLogs() {
        return logSistemaRepository.findTop50ByOrderByFechaHoraDesc();
    }

    /**
     * Obtiene los logs de un usuario específico.
     */
    @Transactional(readOnly = true)
    public List<LogSistema> obtenerLogsPorUsuario(Long usuarioId) {
        return logSistemaRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Obtiene los logs filtrados por nivel.
     */
    @Transactional(readOnly = true)
    public List<LogSistema> obtenerLogsPorNivel(LogSistema.NivelLog nivel) {
        return logSistemaRepository.findByNivel(nivel);
    }

    /**
     * Obtiene los logs dentro de un rango de fechas.
     */
    @Transactional(readOnly = true)
    public List<LogSistema> obtenerLogsPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        return logSistemaRepository.findByFechaHoraBetween(desde, hasta);
    }

    private LogSistema guardarLog(String accion, String descripcion, Usuario usuario, LogSistema.NivelLog nivel) {
        LogSistema log = new LogSistema(accion, descripcion, usuario, nivel);
        LogSistema guardado = logSistemaRepository.save(log);
        logger.info("[{}] {} - {}", nivel, accion, descripcion);
        return guardado;
    }
}
