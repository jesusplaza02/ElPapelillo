
package es.uma.ajdp.tfg.elpapelillo.controllers;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import es.uma.ajdp.tfg.elpapelillo.repositories.*;
import es.uma.ajdp.tfg.elpapelillo.models.*;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.services.DocumentoService;

@RestController
@RequestMapping("/api/documentos")
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentoController {

    @Autowired
    private DocumentoService documentoService;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> subirArchivo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("idInscripcion") @NonNull Integer idInscripcion,
            @RequestParam("nombreDoc") String nombreDoc,
            @RequestParam(value = "tipo", required = false, defaultValue = "PDF") String tipo, // Opcional con valor por defecto
            @RequestParam(value = "usuarioId", required = false) Integer usuarioId) { // Opcional para pruebas en Postman

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Solo se permiten archivos en formato PDF.\"}");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"El archivo supera el límite de 5MB.\"}");
        }

        try {
            String filenameOriginal = file.getOriginalFilename();
            String nombreFisicoLimpio = (filenameOriginal != null)
                    ? System.currentTimeMillis() + "_" + filenameOriginal.replaceAll("\\s+", "_")
                    : "doc_" + System.currentTimeMillis() + ".pdf";

            Path rutaDirectorio = Paths.get("archivos");
            if (!Files.exists(rutaDirectorio)) {
                Files.createDirectories(rutaDirectorio);
            }

            Path rutaArchivo = rutaDirectorio.resolve(nombreFisicoLimpio);
            Files.copy(file.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

            Documento doc = new Documento();
            doc.setNombre(nombreDoc);
            doc.setTipo(tipo);
            doc.setUrlArchivo("archivos/" + nombreFisicoLimpio);
            doc.setEstado(EstadoAdministrativo.PENDIENTE);

            Optional<Inscripcion> inscrip = inscripcionRepository.findById(idInscripcion);
            if (inscrip.isPresent()) {
                doc.setInscripcion(inscrip.get());
            } else {
                Files.deleteIfExists(rutaArchivo);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"Inscripción no encontrada.\"}");
            }

            documentoRepository.save(doc);

            usuarioRepository.findById(usuarioId).ifPresent(user -> {
                if (user instanceof Administrador admin) { // Java 16+ Pattern Matching
                    LogAuditoria log = new LogAuditoria(
                            admin,
                            "ADJUNTAR",
                            "Documento adjunto a Inscripción ID: " + idInscripcion
                    );
                    logAuditoriaRepository.save(log);
                }
            });

            return ResponseEntity.ok().body("{\"message\": \"Archivo subido con éxito\"}");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error al procesar el archivo\"}");
        }
    }

    @GetMapping("/inscripcion/{id}")
    public ResponseEntity<List<Documento>> listarPorInscripcion(@PathVariable Integer id) { // Cambiado a Integer
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        List<Documento> docs = documentoService.listarPorInscripcion(id);
        return ResponseEntity.ok(docs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> evaluarDocumento(
            @PathVariable Integer id, 
            @RequestBody java.util.Map<String, Object> body) {

        return documentoRepository.findById(id).map(docExistente -> {
            
            String nuevoEstadoStr = body.get("estado") != null ? body.get("estado").toString() : "PENDIENTE";
            String nuevoComentario = body.get("comentarioRevision") != null ? body.get("comentarioRevision").toString() : "";

            try {
                EstadoAdministrativo estadoEnum = EstadoAdministrativo.valueOf(nuevoEstadoStr);
                docExistente.setEstado(estadoEnum);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"Estado administrativo no válido.\"}");
            }
            
            docExistente.setComentarioRevision(nuevoComentario);
            
            Documento guardado = documentoRepository.save(docExistente);
            
            return ResponseEntity.ok(guardado); 
            
        }).orElse(ResponseEntity.notFound().build()); 
    }
}