package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.Concurso;
import es.uma.ajdp.tfg.elpapelillo.models.Inscripcion;
import es.uma.ajdp.tfg.elpapelillo.services.ConcursoService;
import es.uma.ajdp.tfg.elpapelillo.services.EmailService;
import es.uma.ajdp.tfg.elpapelillo.repositories.InscripcionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/concursos")
@CrossOrigin(origins = "http://localhost:4200")
public class ConcursoController {

    @Autowired
    private ConcursoService concursoService;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private EmailService emailService;

   @PostMapping(value = "/enviar-circular", consumes = {"multipart/form-data"})
    public ResponseEntity<?> enviarCircularRepresentantes(
            @RequestParam("asunto") String asunto,
            @RequestParam("cuerpo") String cuerpo,
            @RequestParam("idsInscripciones") String idsInscripcionesJson,
            @RequestParam(value = "archivo", required = false) MultipartFile[] archivos) { // 🌟 Cambiado a array []
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Integer> idsInscripciones = mapper.readValue(idsInscripcionesJson, new TypeReference<List<Integer>>(){});

            if (idsInscripciones == null || idsInscripciones.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se han recibido destinatarios válidos."));
            }

            List<Inscripcion> listaInscripciones = inscripcionRepository.findAllById(idsInscripciones);

            Set<String> correosDestinatarios = listaInscripciones.stream()
                .filter(ins -> ins != null && ins.getAgrupacion() != null && ins.getAgrupacion().getRepresentante() != null)
                .map(ins -> ins.getAgrupacion().getRepresentante().getEmail())
                .filter(email -> email != null && !email.trim().isEmpty())
                .collect(Collectors.toSet());

            if (correosDestinatarios.isEmpty()) {
                return ResponseEntity.ok(Map.of("mensaje", "No hay correos válidos registrados para la selección."));
            }

            // 🌟 4. Procesamos los múltiples archivos a listas en memoria antes del hilo asíncrono
            List<byte[]> listaArchivosBytes = new ArrayList<>();
            List<String> listaNombresArchivos = new ArrayList<>();
            
            if (archivos != null && archivos.length > 0) {
                for (MultipartFile file : archivos) {
                    if (file != null && !file.isEmpty()) {
                        listaArchivosBytes.add(file.getBytes());
                        listaNombresArchivos.add(file.getOriginalFilename());
                    }
                }
            }

            // Enviamos las listas completas de adjuntos
            for (String email : correosDestinatarios) {
                emailService.enviarEmailCircularConAdjunto(email, asunto, cuerpo, listaArchivosBytes, listaNombresArchivos);
            }

            return ResponseEntity.ok(Map.of(
                "status", "OK", 
                "mensaje", "Envío masivo iniciado con múltiples adjuntos para " + correosDestinatarios.size() + " destinatarios."
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Fallo al procesar el envío masivo: " + e.getMessage()));
        }
    }
    // Método auxiliar privado para limpiar la lectura del repositorio en el stream
    private List<Inscripcion> srcInscripciones(List<Integer> ids) {
        return inscripcionRepository.findAllById(ids);
    }
    /**
     * Endpoint principal para la tabla de concursos.
     * Filtra automáticamente: 
     * - SYSADMIN: Ve todos.
     * - ADMIN/SUPERADMIN: Solo los de su organización.
     */
    @GetMapping("/mis-concursos/{idUsuarioActual}")
    public ResponseEntity<List<Concurso>> getConcursosPorRol(@PathVariable Integer idUsuarioActual) {
        try {
            List<Concurso> concursos = concursoService.listarConcursosSegunRol(idUsuarioActual);
            return ResponseEntity.ok(concursos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para obtener solo concursos activos (útil para selects o landing page).
     */
    @GetMapping("/activos")
    public ResponseEntity<List<Concurso>> getActivos() {
        return ResponseEntity.ok(concursoService.findActivos());
    }

    /**
     * Ejemplo de cómo podrías tener el detalle de un concurso específico.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Concurso> getById(@PathVariable Integer id) {
        // Asumiendo que añades este método al service después
        return concursoService.findById(id) 
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Concurso> crearConcurso(@RequestBody Concurso concurso) {
        try {
            Concurso nuevo = concursoService.guardar(concurso);
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualizar un concurso existente (PUT)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Concurso> actualizarConcurso(@PathVariable Integer id, @RequestBody Concurso concurso) {
        try {
            // Aseguramos que el objeto tenga el ID correcto antes de guardar
            concurso.setIdConcurso(id); 
            Concurso actualizado = concursoService.guardar(concurso);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            concursoService.eliminarConcurso(id);
            return ResponseEntity.ok().body("{\"message\": \"Concurso eliminado permanentemente\"}");
        } catch (RuntimeException e) {
            // Enviamos el mensaje de error de las reglas (ej: "tiene agrupaciones")
            return ResponseEntity.status(400).body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
}