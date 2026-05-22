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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping(value = "/enviar-circular", consumes = {"multipart/form-data"})
    public ResponseEntity<?> enviarCircularRepresentantes(
            @RequestParam("asunto") String asunto,
            @RequestParam("cuerpo") String cuerpo,
            @RequestParam("idsInscripciones") String idsInscripcionesJson,
            @RequestParam(value = "archivo", required = false) MultipartFile[] archivos) {
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
     * Endpoint para obtener el detalle de un concurso específico de forma verídica.
     * Fuerza la sincronización de Hibernate con el registro real de la base de datos.
     */
    /**
     * Endpoint principal para obtener el detalle de un concurso específico.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Concurso> getById(@PathVariable Integer id) {
        return concursoService.findById(id) 
                .map(concurso -> {
                    // Mapeo correcto con la propiedad de tu entidad
                    System.out.println("====== [BACKEND] ENVIANDO CONCURSO ======");
                    System.out.println("Nombre: " + concurso.getNombre());
                    System.out.println("Estado Real del Enum: " + concurso.getEstadoConcurso());
                    System.out.println("=========================================");
                    return ResponseEntity.ok(concurso);
                })
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
            return ResponseEntity.status(400).body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
}