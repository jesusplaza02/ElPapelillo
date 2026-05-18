
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

    @PutMapping("/{id}")
    public ResponseEntity<?> evaluarDocumento(
            @PathVariable Integer id, 
            @RequestBody java.util.Map<String, Object> body) {
        
        // 1. Buscamos el documento real por su ID en la base de datos
        return documentoRepository.findById(id).map(docExistente -> {
            
            // 2. Extraemos de forma segura el nuevo estado y el comentario que envía Angular
            String nuevoEstadoStr = body.get("estado") != null ? body.get("estado").toString() : "PENDIENTE";
            String nuevoComentario = body.get("comentarioRevision") != null ? body.get("comentarioRevision").toString() : "";
            
            // 3. Convertimos el String del estado al Enum EstadoAdministrativo de tu proyecto
            try {
                EstadoAdministrativo estadoEnum = EstadoAdministrativo.valueOf(nuevoEstadoStr);
                docExistente.setEstado(estadoEnum);
            } catch (IllegalArgumentException e) {
                // Por si acaso llega un estado de Angular que no mapea con el Enum
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"Estado administrativo no válido.\"}");
            }
            
            // 4. Asignamos el comentario de la revisión
            docExistente.setComentarioRevision(nuevoComentario);
            
            // 5. Guardamos de forma definitiva en la base de datos MySQL
            Documento guardado = documentoRepository.save(docExistente);
            
            return ResponseEntity.ok(guardado); // Devolvemos un 200 OK con el documento actualizado
            
        }).orElse(ResponseEntity.notFound().build()); // Si el ID no existe en la BD, lanza un 404
    }
}