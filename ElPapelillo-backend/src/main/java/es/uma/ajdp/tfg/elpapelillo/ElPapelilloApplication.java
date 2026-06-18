package es.uma.ajdp.tfg.elpapelillo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Clase principal que arranca la aplicación "El Papelillo".
 * Las anotaciones extra permiten gestionar correos de forma eficiente.
 */

@SpringBootApplication
@EnableRetry // Permite que el Service reintente el envío de emails si falla el SMTP
@EnableAsync // Permite que el registro de usuario no se quede bloqueado esperando al email
public class ElPapelilloApplication {

    public static void main(String[] args) {
        // Esta línea es la que arranca el servidor TomCat embebido
        SpringApplication.run(ElPapelilloApplication.class, args);
        
        System.out.println("--- Aplicación El Papelillo arrancada correctamente ---");
    }
}