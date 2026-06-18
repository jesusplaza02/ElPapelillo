
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
            @RequestParam(value = "tipo", required = false, defaultValue = "PDF") String tipo, 
            @RequestParam(value = "usuarioId", required = true) Integer usuarioId) { // 🔥 Lo hacemos obligatorio

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Solo se permiten archivos en formato PDF.\"}");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"El archivo supera el límite de 5MB.\"}");
        }

        // --- 🔒 NUEVO FILTRO DE SEGURIDAD ANTES DE GUARDAR NADA ---
        Optional<Inscripcion> inscripOpt = inscripcionRepository.findById(idInscripcion);
        if (inscripOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"Inscripción no encontrada.\"}");
        }

        Inscripcion inscripcion = inscripOpt.get();

        // Validamos que el usuario que intenta subir el archivo sea el verdadero representante
        if (inscripcion.getAgrupacion() != null && inscripcion.getAgrupacion().getRepresentante() != null) {
            Integer idRepresentanteReal = inscripcion.getAgrupacion().getRepresentante().getIdUsuario();
            
            if (!usuarioId.equals(idRepresentanteReal)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"error\": \"Acceso denegado: No puedes añadir documentación a un expediente ajeno.\"}");
            }
        }
        // -----------------------------------------------------------

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
            doc.setInscripcion(inscripcion); // Usamos la que ya encontramos arriba

            documentoRepository.save(doc);

            usuarioRepository.findById(usuarioId).ifPresent(user -> {
                if (user instanceof Administrador admin) { 
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
public ResponseEntity<?> listarPorInscripcion(
        @PathVariable Integer id,
        @RequestParam(value = "idUsuarioActual", required = true) Integer idUsuarioActual) {
    
    if (id == null) {
        return ResponseEntity.badRequest().build();
    }

    Optional<Inscripcion> inscripOpt = inscripcionRepository.findById(id);
    if (inscripOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\": \"Inscripción no encontrada.\"}");
    }

    Inscripcion inscripcion = inscripOpt.get();

    // 1. Verificamos primero si el usuario actual es un ADMINISTRADOR
    boolean esAdmin = usuarioRepository.findById(idUsuarioActual)
            .map(user -> user instanceof Administrador) // Revisa si usas herencia, o cámbialo por: user.getRole().equals("ADMIN")
            .orElse(false);

    // 2. APLICAMOS EL FILTRO INTELIGENTE
    if (!esAdmin) { 
        // Si NO es administrador, obligatoriamente tiene que ser el representante dueño de la agrupación
        if (inscripcion.getAgrupacion() != null && inscripcion.getAgrupacion().getRepresentante() != null) {
            Integer idRepresentanteReal = inscripcion.getAgrupacion().getRepresentante().getIdUsuario();
            
            if (!idUsuarioActual.equals(idRepresentanteReal)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"error\": \"Acceso denegado: No tienes permisos para ver esta agrupación.\"}");
            }
        } else {
            // Si la inscripción no tiene representante asignado y el usuario no es admin, denegamos por seguridad
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Si es Admin o es el Representante dueño, el flujo continúa con éxito:
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