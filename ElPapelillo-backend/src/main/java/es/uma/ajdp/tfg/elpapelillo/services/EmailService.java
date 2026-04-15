package es.uma.ajdp.tfg.elpapelillo.services;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Async // Para que no bloquee el registro
    @Retryable(
        value = { MailException.class }, 
        maxAttempts = 5, 
        backoff = @Backoff(
            delay = 2000, 
            multiplier = 3.0,      // Multiplica el tiempo anterior por 3
            maxDelay = 60000       // Límite máximo de espera: 1 minuto
        )
    )
   public void enviarEmailInstrucciones(String correo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        
        mensaje.setFrom("ElPapelillo@gmail.com");
        mensaje.setTo(correo);
        mensaje.setSubject("Bienvenido a El Papelillo - Instrucciones de Acceso");

        CharacterRule letras = new CharacterRule(EnglishCharacterData.UpperCase, 2);
        CharacterRule digitos = new CharacterRule(EnglishCharacterData.Digit, 2);
        CharacterRule minusculas = new CharacterRule(EnglishCharacterData.LowerCase, 1);

        PasswordGenerator gen = new PasswordGenerator();
        String passwordSinCifrar = gen.generatePassword(10, letras, digitos, minusculas); 
        // Resultado ej: "A1b2C3d4Ef"
        
        mensaje.setText("Hola,\n\n" +
                "Tu cuenta ha sido creada correctamente.\n" +
                "Tus credenciales de acceso son:\n" +
                "Usuario: " + correo + "\n" +
                "Contraseña: " + passwordSinCifrar + "\n\n" +
                "Saludos, el equipo de El Papelillo.");

        mailSender.send(mensaje);
        log.info("Email enviado con éxito a: " + correo);
    }

    /**
     * Este método se ejecuta automáticamente si fallan los 5 intentos
     */
    @Recover
    public void recuperarFalloEmail(MailException e, String correo, String passwordSinCifrar) {
        log.error("LOG CRÍTICO: Imposible enviar email a " + correo + " tras 5 intentos. Error: " + e.getMessage());
    
        // Guardamos en tabla de logs
        LogAuditoria auditoria = new LogAuditoria();
        Administrador sistema = administradorRepository.findByCargo("SISTEMA");
        auditoria.setAdministrador(sistema);
        auditoria.setAccion("ERROR_ENVIO_EMAIL");
        auditoria.setDescripcion("No se pudo enviar credenciales al correo: " + correo);
        // No hace falta setFecha(), el @PrePersist lo hace solo
        
        logAuditoriaRepository.save(auditoria);
    }
}
