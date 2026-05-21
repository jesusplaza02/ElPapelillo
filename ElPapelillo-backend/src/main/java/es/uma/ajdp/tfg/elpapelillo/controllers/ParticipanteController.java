package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.Inscripcion;
import es.uma.ajdp.tfg.elpapelillo.models.Participante;
import es.uma.ajdp.tfg.elpapelillo.models.Participacion;
import es.uma.ajdp.tfg.elpapelillo.models.enums.RolParticipante; // 🔑 Importamos tu Enum correcto
import es.uma.ajdp.tfg.elpapelillo.repositories.InscripcionRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.ParticipanteRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.ParticipacionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/participantes")
@CrossOrigin(origins = "http://localhost:4200")
public class ParticipanteController {

    @Autowired
    private ParticipanteRepository participanteRepository;

    @Autowired
    private ParticipacionRepository participacionRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    /**
     * Obtiene todos los registros de la tabla intermedia asociados a una inscripción concreta.
     */
    @GetMapping("/inscripcion/{idInscripcion}")
    public ResponseEntity<List<Participacion>> obtenerPorInscripcion(@PathVariable Integer idInscripcion) {
        try {
            List<Participacion> lista = participacionRepository.findByInscripcionIdInscripcion(idInscripcion);
            if (lista == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca un participante en el histórico global del sistema por su DNI.
     */
    @GetMapping("/buscar-historico")
    public ResponseEntity<?> buscarPorDni(@RequestParam String dni) {
        try {
            // Buscamos como lista en lugar de Optional para evitar el error "Query did not return a unique result"
            List<Participante> participantes = participanteRepository.findAllByDni(dni.trim());
            
            // Nota: Si no tienes "findAllByDni" en tu ParticipanteRepository, puedes definirlo como:
            // List<Participante> findAllByDni(String dni);
            // O bien, si usas findByDni y devuelve una lista, adáptalo.
            
            if (participantes != null && !participantes.isEmpty()) {
                return ResponseEntity.ok(participantes.get(0)); // Nos quedamos con el registro más antiguo/válido
            }
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            // Fallback de emergencia por si tu repositorio sigue usando findByDni directo:
            try {
                Optional<Participante> pOpt = participanteRepository.findByDni(dni.trim());
                if (pOpt.isPresent()) return ResponseEntity.ok(pOpt.get());
            } catch (Exception ex) {
                System.out.println("Duplicado detectado en el catch de emergencia.");
            }
            
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al buscar en el histórico: " + e.getMessage()));
        }
    }
    /**
     * 💾 GUARDAR / ACTUALIZAR PARTICIPANTE Y PARTICIPACIÓN (CON PARSEO DE ENUM)
     */
    @PostMapping("/guardar")
    public ResponseEntity<?> guardarParticipante(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Extraer datos del JSON de Angular
            Integer idParticipacion = (Integer) payload.get("idParticipacion");
            Integer idParticipanteBase = (Integer) payload.get("idParticipante"); 
            String nombre = (String) payload.get("nombre");
            String dni = ((String) payload.get("dni")).trim();
            String fechaNacStr = (String) payload.get("fechaNacimiento");
            String rolStr = (String) payload.get("rol"); // Viene como texto ("Ayudantes de escena", "Voz"...)
            
            Map<String, Object> inscripcionMap = (Map<String, Object>) payload.get("inscripcion");
            Integer idInscripcion = (Integer) inscripcionMap.get("idInscripcion");

            Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                    .orElseThrow(() -> new RuntimeException("Inscripción no encontrada."));

            // 2. 🛡️ GESTIÓN DEL OBJETO PARTICIPANTE BASE (Evitar Duplicados de DNI)
            Participante participante = null;

            if (idParticipanteBase != null) {
                participante = participanteRepository.findById(idParticipanteBase).orElse(null);
            }

            if (participante == null) {
                Optional<Participante> pExistente = participanteRepository.findByDni(dni);
                if (pExistente.isPresent()) {
                    participante = pExistente.get();
                } else {
                    participante = new Participante();
                }
            }

            participante.setNombre(nombre);
            participante.setDni(dni);
            if (fechaNacStr != null && !fechaNacStr.trim().isEmpty()) {
                participante.setFechaNacimiento(LocalDate.parse(fechaNacStr));
            }
            
            participante = participanteRepository.save(participante);

            // 3. 🔑 TRADUCCIÓN DEL STRING AL ENUM `RolParticipante`
            // Limpia espacios ("Ayudantes de escena" -> "AYUDANTES_DE_ESCENA") y quita acentos
            String rolEnumStr = rolStr.toUpperCase()
                    .replace(" ", "_")
                    .replaceAll("[ÁÁàá]", "A")
                    .replaceAll("[ÉÉèé]", "E")
                    .replaceAll("[ÍÍìí]", "I")
                    .replaceAll("[ÓÓòó]", "O")
                    .replaceAll("[ÚÚùú]", "U");
            
            RolParticipante rolEnum = RolParticipante.valueOf(rolEnumStr);

            // 4. 🔀 GESTIÓN DE LA TABLA INTERMEDIA (Participacion)
            Participacion participacion;
            
            if (idParticipacion != null) {
                participacion = participacionRepository.findById(idParticipacion)
                        .orElse(new Participacion());
                participacion.setIdParticipacion(idParticipacion);
            } else {
                List<Participacion> actuales = participacionRepository.findByInscripcionIdInscripcion(idInscripcion);
                final Integer pId = participante.getId(); 
                boolean yaInscrito = actuales.stream().anyMatch(p -> p.getParticipante().getId().equals(pId));
                
                if (yaInscrito) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Este participante ya está registrado en la lista de esta agrupación."));
                }
                
                participacion = new Participacion();
            }

            participacion.setInscripcion(inscripcion);
            participacion.setParticipante(participante);
            participacion.setRol(rolEnum); // 🔑 ¡Arreglado! Ahora le pasamos la variable de tipo RolParticipante

            participacionRepository.save(participacion);
            
            return ResponseEntity.ok(Map.of("status", "OK", "mensaje", "Participante procesado correctamente."));

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El rol seleccionado '" + payload.get("rol") + "' no coincide con los valores definidos en tu Enum del sistema."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al guardar: " + e.getMessage()));
        }
    }

    /**
     * 🗑️ ELIMINAR PARTICIPACIÓN DE LA INSCRIPCIÓN
     */
    @DeleteMapping("/eliminar/{idParticipacion}")
    public ResponseEntity<?> eliminarParticipante(@PathVariable Integer idParticipacion) {
        try {
            Optional<Participacion> participacionOpt = participacionRepository.findById(idParticipacion);
            if (participacionOpt.isPresent()) {
                participacionRepository.delete(participacionOpt.get());
                return ResponseEntity.ok(Map.of("status", "OK", "mensaje", "El participante ha sido desvinculado con éxito."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No se encontró el registro de vinculación solicitado."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo eliminar el registro debido a un error en la base de datos."));
        }
    }
}