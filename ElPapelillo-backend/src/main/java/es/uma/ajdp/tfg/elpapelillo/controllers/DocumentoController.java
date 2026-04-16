package es.uma.ajdp.tfg.elpapelillo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.uma.ajdp.tfg.elpapelillo.models.Documento;
import es.uma.ajdp.tfg.elpapelillo.services.DocumentoService;

@RestController
@RequestMapping("/api/documentos")
@CrossOrigin(origins = "http://localhost:4200") // Para permitir peticiones desde Angular
public class DocumentoController {

    @Autowired
    private DocumentoService documentoService;

    /**
     * Endpoint para subir archivos desde la página de documentación
     */
    @PostMapping("/upload")
    public ResponseEntity<?> subirDocumento(
            @RequestParam("file") MultipartFile file,
            @RequestParam("idAgrupacion") Long idAgrupacion,
            @RequestParam("nombreDoc") String nombreDoc) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("El archivo está vacío");
            }

            // Llamamos al servicio para procesar el archivo y guardar en DB
            Documento nuevoDoc = documentoService.guardarDocumento(file, idAgrupacion, nombreDoc);
            
            return ResponseEntity.ok(nuevoDoc);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir el archivo: " + e.getMessage());
        }
    }

    /**
     * Endpoint para listar los documentos de una agrupación específica
     */
    @GetMapping("/agrupacion/{id}")
    public ResponseEntity<List<Documento>> listarPorAgrupacion(@PathVariable Long id) {
        List<Documento> docs = documentoService.listarPorAgrupacion(id);
        return ResponseEntity.ok(docs);
    }
}