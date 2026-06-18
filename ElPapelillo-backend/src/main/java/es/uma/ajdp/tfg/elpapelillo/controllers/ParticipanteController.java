package es.uma.ajdp.tfg.elpapelillo.controllers;

import es.uma.ajdp.tfg.elpapelillo.models.Inscripcion;
import es.uma.ajdp.tfg.elpapelillo.models.Participante;
import es.uma.ajdp.tfg.elpapelillo.models.Participacion;
import es.uma.ajdp.tfg.elpapelillo.models.Administrador;
import es.uma.ajdp.tfg.elpapelillo.models.enums.RolParticipante;
import es.uma.ajdp.tfg.elpapelillo.repositories.InscripcionRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.ParticipanteRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.ParticipacionRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.UsuarioRepository;

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

    @Autowired
    private UsuarioRepository usuarioRepository;

    private boolean validarAccesoInscripcion(Integer idInscripcion, Integer idUsuarioActual) {
        if (idInscripcion == null || idUsuarioActual == null) {
            return false;
        }
        
        boolean esAdmin = usuarioRepository.findById(idUsuarioActual)
                .map(user -> user instanceof Administrador)
                .orElse(false);
                
        if (esAdmin) {
            return true;
        }

        Optional<Inscripcion> inscripOpt = inscripcionRepository.findById(idInscripcion);
        if (inscripOpt.isEmpty()) {
            return false;
        }

        Inscripcion inscripcion = inscripOpt.get();
        if (inscripcion.getAgrupacion() != null && inscripcion.getAgrupacion().getRepresentante() != null) {
            Integer idRepresentanteReal = inscripcion.getAgrupacion().getRepresentante().getIdUsuario();
            return idUsuarioActual.equals(idRepresentanteReal);
        }
        
        return false;
    }

    @GetMapping("/inscripcion/{idInscripcion}")
    public ResponseEntity<?> obtenerPorInscripcion(
            @PathVariable Integer idInscripcion,
            @RequestParam(value = "idUsuarioActual", required = true) Integer idUsuarioActual) {
        try {
            if (!validarAccesoInscripcion(idInscripcion, idUsuarioActual)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: No tienes permisos para ver estos participantes."));
            }

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

    @GetMapping("/buscar-historico")
    public ResponseEntity<?> buscarPorDni(
            @RequestParam String dni,
            @RequestParam(value = "idUsuarioActual", required = true) Integer idUsuarioActual) {
        try {
            boolean esAdmin = usuarioRepository.findById(idUsuarioActual)
                    .map(user -> user instanceof Administrador)
                    .orElse(false);
                    
            if (!esAdmin && usuarioRepository.findById(idUsuarioActual).isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: Sesión no válida."));
            }

            String dniCifrado = es.uma.ajdp.tfg.elpapelillo.util.CryptoUtil.encrypt(dni.trim());
            List<Participante> participantes = participanteRepository.findAllByDni(dniCifrado);
            
            if (participantes != null && !participantes.isEmpty()) {
                return ResponseEntity.ok(participantes.get(0));
            }
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            try {
                String dniCifrado = es.uma.ajdp.tfg.elpapelillo.util.CryptoUtil.encrypt(dni.trim());
                Optional<Participante> pOpt = participanteRepository.findByDni(dniCifrado);
                if (pOpt.isPresent()) return ResponseEntity.ok(pOpt.get());
            } catch (Exception ex) {
                System.out.println("Duplicado detectado en el catch de emergencia.");
            }
            
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al buscar en el histórico: " + e.getMessage()));
        }
    }
   
    @PostMapping("/guardar")
    public ResponseEntity<?> guardarParticipante(@RequestBody Map<String, Object> payload) {
        try {
            Integer idUsuarioActual = (Integer) payload.get("idUsuarioActual");
            Map<String, Object> inscripcionMap = (Map<String, Object>) payload.get("inscripcion");
            Integer idInscripcion = (Integer) inscripcionMap.get("idInscripcion");

            if (!validarAccesoInscripcion(idInscripcion, idUsuarioActual)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: No tienes permisos para modificar esta inscripción."));
            }

            Integer idParticipacion = (Integer) payload.get("idParticipacion");
            Integer idParticipanteBase = (Integer) payload.get("idParticipante"); 
            String nombre = (String) payload.get("nombre");
            String dni = ((String) payload.get("dni")).trim();
            String fechaNacStr = (String) payload.get("fechaNacimiento");
            String rolStr = (String) payload.get("rol"); 

            Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                    .orElseThrow(() -> new RuntimeException("Inscripción no encontrada."));

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

            String rolEnumStr = rolStr.toUpperCase()
                    .replace(" ", "_")
                    .replaceAll("[ÁÁàá]", "A")
                    .replaceAll("[ÉÉèé]", "E")
                    .replaceAll("[ÍÍìí]", "I")
                    .replaceAll("[ÓÓòó]", "O")
                    .replaceAll("[ÚÚùú]", "U");
            
            RolParticipante rolEnum = RolParticipante.valueOf(rolEnumStr);

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
            participacion.setRol(rolEnum); 

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

    @DeleteMapping("/eliminar/{idParticipacion}")
    public ResponseEntity<?> eliminarParticipante(
            @PathVariable Integer idParticipacion,
            @RequestParam(value = "idUsuarioActual", required = true) Integer idUsuarioActual) {
        try {
            Optional<Participacion> participacionOpt = participacionRepository.findById(idParticipacion);
            if (participacionOpt.isPresent()) {
                Participacion part = participacionOpt.get();
                Integer idInscripcion = part.getInscripcion().getIdInscripcion();

                if (!validarAccesoInscripcion(idInscripcion, idUsuarioActual)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Acceso denegado: No tienes permisos para eliminar este participante."));
                }

                participacionRepository.delete(part);
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