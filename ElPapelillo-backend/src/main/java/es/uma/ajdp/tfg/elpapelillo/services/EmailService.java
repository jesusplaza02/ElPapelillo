package es.uma.ajdp.tfg.elpapelillo.services;

import java.util.List;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import es.uma.ajdp.tfg.elpapelillo.models.Administrador;
import es.uma.ajdp.tfg.elpapelillo.models.LogAuditoria;
import es.uma.ajdp.tfg.elpapelillo.repositories.AdministradorRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.LogAuditoriaRepository;
import es.uma.ajdp.tfg.elpapelillo.util.CryptoUtil; // 🌟 Importamos tu utilidad de cifrado
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @SuppressWarnings("deprecation")
    @Async 
    @Retryable(
        value = { MailException.class }, 
        maxAttempts = 5, 
        backoff = @Backoff(
            delay = 2000, 
            multiplier = 3.0,      
            maxDelay = 60000       
        )
    )
    public void enviarEmailInstrucciones(String correoParam, String passwordSinCifrar) { 
        // 🌟 ARREGLO: Nos aseguramos de descifrar el correo por si acaso viene de un @PrePersist
        String correoLimpio = CryptoUtil.decrypt(correoParam);

        log.info("Intentando enviar credenciales al destinatario: '{}'", correoLimpio);

        SimpleMailMessage mensaje = new SimpleMailMessage();
        
        mensaje.setFrom("ElPapelillo@gmail.com");
        mensaje.setTo(correoLimpio); // 🌟 Usamos el correo descifrado en texto plano
        mensaje.setSubject("Bienvenido a El Papelillo - Instrucciones de Acceso");

        mensaje.setText("Hola,\n\n" +
                "Tu cuenta ha sido creada correctamente.\n" +
                "Tus credenciales de acceso son:\n" +
                "Usuario: " + correoLimpio + "\n" + // 🌟 Evitamos mostrar el hash en el texto del mensaje
                "Contraseña: " + passwordSinCifrar + "\n\n" +
                "Saludos, el equipo de El Papelillo.");

        mailSender.send(mensaje);
        log.info("Email enviado con éxito a: " + correoLimpio);
    }

    @Recover
    public void recuperarFalloEmail(MailException e, String correoParam, String passwordSinCifrar) {
        String correoLimpio = CryptoUtil.decrypt(correoParam);
        log.error("LOG CRÍTICO: Imposible enviar email a " + correoLimpio + " tras 5 intentos. Error: " + e.getMessage());
    
        LogAuditoria auditoria = new LogAuditoria();
        Administrador sistema = administradorRepository.findByCargo("SISTEMA");
        auditoria.setAdministrador(sistema);
        auditoria.setAccion("ERROR_ENVIO_EMAIL");
        auditoria.setDescripcion("No se pudo enviar credenciales al correo: " + correoLimpio);
        
        logAuditoriaRepository.save(auditoria);
    }

    @Async
    @Retryable(
        value = { Exception.class }, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 2000)
    )
    public void enviarEmailCircularConAdjunto(String correoDestinatarioParam, String asunto, String cuerpo, 
                                             List<byte[]> listaArchivosBytes, List<String> listaNombresArchivos) {
        // 🌟 ARREGLO: Blindamos también el envío masivo de circulares
        String correoDestinatarioLimpio = CryptoUtil.decrypt(correoDestinatarioParam);

        try {
            log.info("Preparando envío de circular masiva para: '{}'", correoDestinatarioLimpio);

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            
            helper.setFrom("ElPapelillo@gmail.com");
            helper.setTo(correoDestinatarioLimpio); // 🌟 Usamos el correo descifrado
            helper.setSubject(asunto);
            helper.setText(cuerpo, false); 

            if (listaArchivosBytes != null && !listaArchivosBytes.isEmpty()) {
                for (int i = 0; i < listaArchivosBytes.size(); i++) {
                    byte[] bytes = listaArchivosBytes.get(i);
                    String nombre = listaNombresArchivos.get(i);
                    
                    if (bytes != null && bytes.length > 0 && nombre != null) {
                        ByteArrayResource recursoAdjunto = new ByteArrayResource(bytes);
                        helper.addAttachment(nombre, recursoAdjunto);
                    }
                }
            }

            mailSender.send(mensaje);
            log.info("Circular masiva enviada con (" + (listaArchivosBytes != null ? listaArchivosBytes.size() : 0) + ") adjuntos a: " + correoDestinatarioLimpio);
            
        } catch (Exception e) {
            log.error("Error al estructurar correo con múltiples adjuntos para: " + correoDestinatarioLimpio, e);
            throw new RuntimeException(e); 
        }
    }
}