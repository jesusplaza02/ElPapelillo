package es.uma.ajdp.tfg.elpapelillo.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path; // CORRECTO: Para manejo de archivos
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import es.uma.ajdp.tfg.elpapelillo.repositories.*;
import es.uma.ajdp.tfg.elpapelillo.models.Agrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.Documento;
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
    private AgrupacionRepository agrupacionRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> subirArchivo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("idAgrupacion") Integer idAgrupacion,
            @RequestParam("nombreDoc") String nombreDoc,
            @RequestParam("tipo") String tipo) {

        try {
            // 1. Limpiamos el nombre del archivo para el sistema de archivos
            String filenameOriginal = file.getOriginalFilename();
            String nombreFisicoLimpio = (filenameOriginal != null) 
                ? filenameOriginal.replaceAll("\\s+", "_") 
                : "archivo_" + System.currentTimeMillis();

            // 2. Definimos y creamos la carpeta 'archivos' si no existe
            Path rutaDirectorio = Paths.get("archivos");
            if (!Files.exists(rutaDirectorio)) {
                Files.createDirectories(rutaDirectorio);
            }

            // 3. Guardamos físicamente el archivo en el disco
            Path rutaArchivo = rutaDirectorio.resolve(nombreFisicoLimpio);
            Files.copy(file.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

            // 4. Creamos el objeto Documento para la Base de Datos
            Documento doc = new Documento();
            
            // Usamos el nombre y tipo recibidos desde el formulario de Angular
            doc.setNombre(nombreDoc); 
            doc.setTipo(tipo); 
            
            // Guardamos la ruta relativa para acceder luego
            doc.setUrlArchivo("archivos/" + nombreFisicoLimpio); 
            
            // Usamos el Enum correspondiente
            doc.setEstado(EstadoAdministrativo.PENDIENTE);

            if (idAgrupacion == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"El ID de agrupación no puede ser nulo\"}");
            }
            // Buscamos la agrupación por su ID para establecer la relación
            Optional<Agrupacion> agrup = agrupacionRepository.findById(idAgrupacion);
            if (agrup.isPresent()) {
                doc.setAgrupacion(agrup.get());
            } else {
                return ResponseEntity.badRequest().body("{\"error\": \"No se encontró la agrupación con ID: " + idAgrupacion + "\"}");
            }

            // Guardamos el registro en la BD
            documentoRepository.save(doc);

            return ResponseEntity.ok().body("{\"message\": \"Archivo subido y registrado con éxito\"}");

        } catch (IOException e) {
            return ResponseEntity.status(500).body("{\"error\": \"Error técnico al guardar el archivo: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/agrupacion/{id}")
    public ResponseEntity<List<Documento>> listarPorAgrupacion(@PathVariable Long id) {
        List<Documento> docs = documentoService.listarPorAgrupacion(id);
        return ResponseEntity.ok(docs);
    }
}