
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
            @RequestParam("idInscripcion") @NonNull Integer idInscripcion, // Cambiado a Integer
            @RequestParam("nombreDoc") String nombreDoc,
            @RequestParam("tipo") String tipo,
            @RequestParam("usuarioId") @NonNull Integer usuarioId) {

        // --- VALIDACIÓN RF21: Solo PDF y máximo 5MB ---
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
            // 1. Gestión de archivo físico
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

            // 2. Crear y configurar la entidad Documento
            Documento doc = new Documento();
            doc.setNombre(nombreDoc);
            doc.setTipo(tipo);
            doc.setUrlArchivo("archivos/" + nombreFisicoLimpio);
            doc.setEstado(EstadoAdministrativo.PENDIENTE);

            // 3. Vincular con la Inscripción (Usando Integer)
            // Esto quita el error de "Cast argument to int" de tu IDE
            Optional<Inscripcion> inscrip = inscripcionRepository.findById(idInscripcion);
            if (inscrip.isPresent()) {
                doc.setInscripcion(inscrip.get());
            } else {
                Files.deleteIfExists(rutaArchivo);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"Inscripción no encontrada.\"}");
            }

            documentoRepository.save(doc);

            // --- LÓGICA DE AUDITORÍA ---
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
}