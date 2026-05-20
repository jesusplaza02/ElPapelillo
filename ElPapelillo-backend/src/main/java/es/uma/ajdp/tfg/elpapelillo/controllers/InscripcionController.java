package es.uma.ajdp.tfg.elpapelillo.controllers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.uma.ajdp.tfg.elpapelillo.models.Inscripcion;
import es.uma.ajdp.tfg.elpapelillo.repositories.AgrupacionRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.ConcursoRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.InscripcionRepository;
import es.uma.ajdp.tfg.elpapelillo.services.InscripcionService;
import es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion;
import es.uma.ajdp.tfg.elpapelillo.models.enums.ModalidadDioses;



@RestController
@RequestMapping("/api/inscripciones")
@CrossOrigin(origins = "http://localhost:4200") // Evita problemas de CORS con Angular
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private InscripcionRepository inscripcionRepository;
    
    @Autowired
    private AgrupacionRepository agrupacionRepository; 

     @Autowired
    private ConcursoRepository concursoRepository; 


    // 1. Obtener inscripciones de un representante
   @GetMapping("/representante/{idRepresentante}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true) // 🔑 LA SOLUCIÓN AQUÍ
    public ResponseEntity<List<Inscripcion>> getInscripcionesRepresentante(@PathVariable Integer idRepresentante) {
        
        // Tu lógica original (sin los System.out.println del toString() que rompían)
        List<Inscripcion> inscripciones = inscripcionService.obtenerInscripcionesPorRepresentante(idRepresentante);
        
        return ResponseEntity.ok(inscripciones);
    }
    // 2. Obtener inscripciones de un concurso concreto
    @GetMapping("/concurso/{idConcurso}")
    public ResponseEntity<List<Inscripcion>> getInscripcionesConcurso(@PathVariable Integer idConcurso) {
        List<Inscripcion> inscripciones = inscripcionService.obtenerInscripcionesPorConcurso(idConcurso);
        return ResponseEntity.ok(inscripciones);
    }

    // 3. Crear una nueva inscripción
   @PostMapping(consumes = "application/json")
    public ResponseEntity<?> crearInscripcion(@RequestBody java.util.Map<String, Object> payload) {
        try {
            Inscripcion nuevaInscripcion = new Inscripcion();
            
            // ==========================================================
            // 1. VINCULAR Y CARGAR EL CONCURSO REAL DE LA BD
            // ==========================================================
            es.uma.ajdp.tfg.elpapelillo.models.Concurso concursoReal = null;
            if (payload.containsKey("concurso") && payload.get("concurso") != null) {
                java.util.Map<String, Object> concursoMap = (java.util.Map<String, Object>) payload.get("concurso");
                if (concursoMap.containsKey("idConcurso") && concursoMap.get("idConcurso") != null) {
                    int idConcurso = ((Number) concursoMap.get("idConcurso")).intValue();
                    
                    // Buscamos el concurso completo en la BD para tener su fechaInicio
                    concursoReal = concursoRepository.findById(idConcurso).orElse(null);
                    if (concursoReal != null) {
                        nuevaInscripcion.setConcurso(concursoReal);
                    }
                }
            }
            
            if (concursoReal == null) {
                return ResponseEntity.badRequest().body("El concurso especificado no existe o es obligatorio.");
            }
            
            // ==========================================================
            // 🔥 SOLUCIÓN SEGURA PARA EXTRAER EL AÑO DE LA FECHA
            // ==========================================================
            Integer anioConcurso = null;
            if (concursoReal.getFechaInicio() != null) {
                Object fechaObj = concursoReal.getFechaInicio();
                
                if (fechaObj instanceof java.util.Date) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime((java.util.Date) fechaObj);
                    anioConcurso = cal.get(java.util.Calendar.YEAR);
                } else if (fechaObj instanceof java.time.temporal.TemporalAccessor) {
                    // Captura de forma segura cualquier tipo de java.time (LocalDate, LocalDateTime, ZonedDateTime, etc.)
                    anioConcurso = ((java.time.temporal.TemporalAccessor) fechaObj).get(java.time.temporal.ChronoField.YEAR);
                }
            }
            
            // ==========================================================
            // 2. CONSTRUIR Y MAPEAR LA AGRUPACIÓN
            // ==========================================================
            if (payload.containsKey("agrupacion") && payload.get("agrupacion") != null) {
                java.util.Map<String, Object> agrupacionMap = (java.util.Map<String, Object>) payload.get("agrupacion");
                
                es.uma.ajdp.tfg.elpapelillo.models.Agrupacion agrupacionFinal = null;

                String tipoStr = (String) agrupacionMap.get("tipo"); 
                if (tipoStr == null) {
                    tipoStr = (String) agrupacionMap.get("tipoConcurso");
                }

                String nombreAgrupacion = (String) agrupacionMap.get("nombre");

                // 🛑 VALIDACIÓN: Nombre único por concurso vía Repositorio
                if (nombreAgrupacion != null) {
                    boolean yaExisteNombre = inscripcionRepository.existsByConcurso_IdConcursoAndAgrupacion_NombreIgnoreCase(
                        concursoReal.getIdConcurso(), 
                        nombreAgrupacion
                    );
                    
                    if (yaExisteNombre) {
                        return ResponseEntity.badRequest().body("Ya existe una agrupación inscrita con el nombre '" + nombreAgrupacion + "' en este concurso.");
                    }
                }

                boolean esNuevaAgrupacion = (agrupacionMap.get("idAgrupacion") == null);

                if (!esNuevaAgrupacion) {
                    // ----------------------------------------------------------
                    // CASO A: REUTILIZAR EXISTENTE
                    // ----------------------------------------------------------
                    agrupacionFinal = new es.uma.ajdp.tfg.elpapelillo.models.Agrupacion() {};
                    agrupacionFinal.setIdAgrupacion(((Number) agrupacionMap.get("idAgrupacion")).intValue());
                    
                } else {
                    // ----------------------------------------------------------
                    // CASO B: CREAR NUEVA AGRUPACIÓN DESDE CERO SEGÚN SU SUBTIPO
                    // ----------------------------------------------------------
                    java.util.Map<String, Object> camposEspecificosMap = agrupacionMap;
                    
                    if ("CANTO".equalsIgnoreCase(tipoStr)) {
                        es.uma.ajdp.tfg.elpapelillo.models.AgrupacionCanto canto = new es.uma.ajdp.tfg.elpapelillo.models.AgrupacionCanto();
                        
                        if (agrupacionMap.containsKey("agrupacionCanto")) {
                            camposEspecificosMap = (java.util.Map<String, Object>) agrupacionMap.get("agrupacionCanto");
                        }
                        
                        if (camposEspecificosMap != null) {
                            canto.setAutorLetra((String) camposEspecificosMap.get("autorLetra"));
                            canto.setAutorMusica((String) camposEspecificosMap.get("autorMusica"));
                            canto.setDireccion((String) camposEspecificosMap.get("direccion"));
                            
                            // Guardar la Modalidad de Canto (Enum ModalidadCanto)
                            String modStr = (String) camposEspecificosMap.get("modalidad");
                            if (modStr == null) {
                                modStr = (String) payload.get("modalidad"); // Por si viene en la raíz del payload de Angular
                            }
                            if (modStr != null) {
                                try {
                                    canto.setModalidad(es.uma.ajdp.tfg.elpapelillo.models.enums.ModalidadCanto.valueOf(modStr.toUpperCase()));
                                } catch (Exception e) {
                                    System.err.println("No se pudo mapear la modalidad de canto: " + modStr);
                                }
                            }
                        }
                        agrupacionFinal = canto;
                        
                    } else if ("DRAG".equalsIgnoreCase(tipoStr)) {
                        es.uma.ajdp.tfg.elpapelillo.models.AgrupacionDrag drag = new es.uma.ajdp.tfg.elpapelillo.models.AgrupacionDrag();
                        
                        if (agrupacionMap.containsKey("agrupacionDrag")) {
                            camposEspecificosMap = (java.util.Map<String, Object>) agrupacionMap.get("agrupacionDrag");
                        }
                        
                        if (camposEspecificosMap != null) {
                            drag.setNombreArtisticoDrag((String) camposEspecificosMap.get("nombreArtisticoDrag"));
                            drag.setDisenador((String) camposEspecificosMap.get("disenador"));
                        }
                        agrupacionFinal = drag;
                        
                    } else if ("DIOSES".equalsIgnoreCase(tipoStr)) {
                        es.uma.ajdp.tfg.elpapelillo.models.AgrupacionDioses dioses = new es.uma.ajdp.tfg.elpapelillo.models.AgrupacionDioses();
                        
                        if (agrupacionMap.containsKey("agrupacionDioses")) {
                            camposEspecificosMap = (java.util.Map<String, Object>) agrupacionMap.get("agrupacionDioses");
                        }
                        
                        if (camposEspecificosMap != null) {
                            dioses.setModelo((String) camposEspecificosMap.get("modelo"));
                            dioses.setDisenador((String) camposEspecificosMap.get("disenador"));
                            
                            // Guardar la Modalidad de Dios/Diosa (Enum ModalidadDios)
                            String modDiosStr = (String) camposEspecificosMap.get("modalidadDios");
                            if (modDiosStr == null) {
                                modDiosStr = (String) camposEspecificosMap.get("categoria"); 
                            }
                            if (modDiosStr == null) {
                                modDiosStr = (String) payload.get("modalidadDios"); // Respaldo raíz
                            }
                            if (modDiosStr != null) {
                                try {
                                    dioses.setModalidad(es.uma.ajdp.tfg.elpapelillo.models.enums.ModalidadDioses.valueOf(modDiosStr.toUpperCase()));
                                } catch (Exception e) {
                                    System.err.println("No se pudo mapear el tipo de Dios: " + modDiosStr);
                                }
                            }
                        }
                        agrupacionFinal = dioses;
                        
                    } else {
                        // 🚀 CASO "OTRO": Instanciar la clase real heredada
                        es.uma.ajdp.tfg.elpapelillo.models.AgrupacionOtros otros = new es.uma.ajdp.tfg.elpapelillo.models.AgrupacionOtros();
                        
                        if (agrupacionMap.containsKey("agrupacionOtros")) {
                            camposEspecificosMap = (java.util.Map<String, Object>) agrupacionMap.get("agrupacionOtros");
                        } else {
                            camposEspecificosMap = payload; // Por si Angular lo envía suuelto en la raíz
                        }
                        
                        if (camposEspecificosMap != null) {
                            otros.setComentariosDestacables((String) camposEspecificosMap.get("comentariosDestacables"));
                        }
                        agrupacionFinal = otros;
                    }
                }

                // Volcado de campos comunes
                if (agrupacionFinal != null) {
                    agrupacionFinal.setNombre(nombreAgrupacion);
                    if (agrupacionMap.get("nombreUltimaParticipacion") != null) {
                        agrupacionFinal.setNombreUltimaParticipacion((String) agrupacionMap.get("nombreUltimaParticipacion"));
                    }
                    
                    // 🔥 ASIGNACIÓN AUTOMÁTICA DEL AÑO: Extraído del concurso
                    if (anioConcurso != null) {
                        agrupacionFinal.setAnio(anioConcurso);
                    } else if (agrupacionMap.get("anio") != null) {
                        agrupacionFinal.setAnio(((Number) agrupacionMap.get("anio")).intValue());
                    }
                    
                    if (tipoStr != null) {
                        try {
                            agrupacionFinal.setTipoConcurso(es.uma.ajdp.tfg.elpapelillo.models.enums.TipoConcurso.valueOf(tipoStr.toUpperCase()));
                        } catch (Exception e) {
                            System.err.println("No se pudo mapear el tipo de concurso: " + tipoStr);
                        }
                    }

                    // Vincular Representante
                    if (agrupacionMap.containsKey("representante") && agrupacionMap.get("representante") != null) {
                        java.util.Map<String, Object> repMap = (java.util.Map<String, Object>) agrupacionMap.get("representante");
                        if (repMap.get("idUsuario") != null) {
                            es.uma.ajdp.tfg.elpapelillo.models.Representante rep = new es.uma.ajdp.tfg.elpapelillo.models.Representante();
                            rep.setIdUsuario(((Number) repMap.get("idUsuario")).intValue());
                            try {
                                agrupacionFinal.setRepresentante(rep);
                            } catch (Exception e) {
                                System.err.println("No se pudo asignar el representante.");
                            }
                        }
                    }

                    // Mapear Categoría General de la Agrupación (Adulto, Juvenil, etc.)
                    if (agrupacionMap.get("categoria") != null) {
                        String catStr = (String) agrupacionMap.get("categoria");
                        try {
                            agrupacionFinal.setCategoria(es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion.valueOf(catStr.toUpperCase()));
                        } catch (Exception e) {
                            // Ignorar si choca con valores temporales
                        }
                    }

                    // 🔥 PERSISTENCIA PREVIA: Guardar la agrupación en la BD antes de enlazar la inscripción
                    if (esNuevaAgrupacion) {
                        agrupacionFinal = agrupacionRepository.save(agrupacionFinal);
                    }

                    nuevaInscripcion.setAgrupacion(agrupacionFinal);
                }
            }
            
            // ==========================================================
            // 3. PERSISTIR LA INSCRIPCIÓN FINAL
            // ==========================================================
            Inscripcion guardada = inscripcionService.crearInscripcion(nuevaInscripcion);
            return ResponseEntity.ok(guardada);
            
        } catch (Exception e) {
            System.err.println("Error crítico en InscripcionController:");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }
    // ===================================================================
    // NUEVO: Obtener una inscripción individual por su ID para el detalle
    // ===================================================================
    @GetMapping("/{id}")
    public ResponseEntity<Inscripcion> getInscripcionPorId(@PathVariable Integer id) {
        Inscripcion inscripcion = inscripcionService.obtenerInscripcionPorId(id);
        
        if (inscripcion != null) {
            return ResponseEntity.ok(inscripcion);
        } else {
            return ResponseEntity.notFound().build(); // Devuelve 404 si el ID no existe en la BD
        }
    }

    // ===================================================================
    // NUEVO: Actualizar el estado de la inscripción (APROBADO/RECHAZADO)
    // ===================================================================
    @PutMapping("/{id}/estado")
    public ResponseEntity<Inscripcion> actualizarEstadoInscripcion(
            @PathVariable Integer id, 
            @RequestBody Map<String, String> body) {
        
        String nuevoEstado = body.get("estado");
        Inscripcion actualizada = inscripcionService.cambiarEstadoInscripcion(id, nuevoEstado);
        
        if (actualizada != null) {
            return ResponseEntity.ok(actualizada);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/{id}/exportar-pdf")
    public ResponseEntity<byte[]> exportarListadoComponentesPdf(@PathVariable Integer id) {
        try {
            Inscripcion inscripcion = inscripcionService.obtenerInscripcionPorId(id);
            if (inscripcion == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 1. Llamamos al generador real que usa com.lowagie.text
            byte[] pdfBytes = inscripcionService.generarPdfComponentes(inscripcion);
            
            // 2. Configuramos las cabeceras HTTP usando clases puras de Spring
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            
            // Reemplazamos espacios por guiones bajos para que el nombre del archivo no se rompa
            String nombreAgrupacion = "Agrupacion";
            if (inscripcion.getAgrupacion() != null && inscripcion.getAgrupacion().getNombre() != null) {
                nombreAgrupacion = inscripcion.getAgrupacion().getNombre().replace(" ", "_");
            }
            
            String nombreArchivo = "Listado_" + nombreAgrupacion + ".pdf";
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            
            // 3. Devolvemos la respuesta con estado 200 OK y los bytes del PDF
            return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("Error en el controlador al exportar PDF: " + e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================================================
    // NUEVO 7: Exportar el Listado Resumen General de todo el Concurso (Por ID)
    // =========================================================================
    @PostMapping("/exportar-pdf-general")
    public ResponseEntity<byte[]> descargarPdfGeneral(
            @RequestParam("idConcurso") Long idConcurso,
            @RequestParam("nombreConcurso") String nombreConcurso) {
        try {
            // El propio backend recupera las inscripciones limpias de la base de datos
            List<Inscripcion> inscripciones = inscripcionService.obtenerInscripcionesPorConcurso(idConcurso); 

            byte[] pdfBytes = inscripcionService.generarPdfGeneralConcurso(nombreConcurso, inscripciones);
            
            if (pdfBytes == null || pdfBytes.length == 0) {
                return ResponseEntity.noContent().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Listado_General_Concurso.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================================================
    // NUEVO 8: Exportar fichas de los componentes de grupos SELECCIONADOS (Checkbox)
    // =========================================================================
   @PostMapping("/exportar-pdf-seleccionados")
public ResponseEntity<byte[]> exportarPdfSeleccionados(@RequestBody List<Integer> idsInscripcionesInt) {
    try {
        if (idsInscripcionesInt == null || idsInscripcionesInt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Le pasamos la lista de Integer directamente al servicio, SIN conversiones raras
        byte[] pdfBytes = inscripcionService.generarPdfSeleccionadosPorIds(idsInscripcionesInt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Fichas_Componentes_Seleccionados.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

    } catch (Exception e) {
        System.err.println("Error al exportar seleccionados: " + e.getMessage());
        e.printStackTrace();
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

}