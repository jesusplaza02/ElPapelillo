package es.uma.ajdp.tfg.elpapelillo.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import es.uma.ajdp.tfg.elpapelillo.models.Fianza;
import es.uma.ajdp.tfg.elpapelillo.services.FianzaService;

@RestController
@RequestMapping("/api/fianzas")
@CrossOrigin(origins = "http://localhost:4200")
public class FianzaController {

    @Autowired
    private FianzaService fianzaService;

   @PostMapping("/upload/{idInscripcion}")
    public ResponseEntity<?> subirFianza(
            @PathVariable Integer idInscripcion,
            @RequestParam("file") MultipartFile file,
            @RequestParam("importe") Double importe,
            @RequestParam("fechaPago") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaPago) { 
            // ¡Al poner LocalDateTime directamente con la anotación, Spring Boot hace la magia solo!

        try {
            // Llamamos al servicio pasando los nuevos datos directos
            Fianza fianzaProcesada = fianzaService.subirseFianza(idInscripcion, file, importe, fechaPago);
            return ResponseEntity.ok(fianzaProcesada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/inscripcion/{idInscripcion}")
    public ResponseEntity<?> eliminarFianzaPorInscripcion(@PathVariable Integer idInscripcion) {
        try {
            // Tu lógica para desvincular la fianza de la inscripción y borrarla
            fianzaService.eliminarFianzaPorInscripcion(idInscripcion); 
            return ResponseEntity.ok().body("{\"mensaje\": \"Fianza eliminada correctamente\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}