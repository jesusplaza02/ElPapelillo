package es.uma.ajdp.tfg.elpapelillo.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import es.uma.ajdp.tfg.elpapelillo.models.Administrador;
import es.uma.ajdp.tfg.elpapelillo.models.Concurso;
import es.uma.ajdp.tfg.elpapelillo.repositories.AgrupacionRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.ConcursoRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.InscripcionRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.UsuarioRepository;
import es.uma.ajdp.tfg.elpapelillo.services.ConcursoService;
import es.uma.ajdp.tfg.elpapelillo.services.InscripcionService;

@RestController
@RequestMapping("/api/inscripciones")
@CrossOrigin(origins = "http://localhost:4200") // Evita problemas de CORS con Angular
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private InscripcionRepository inscripcionRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository; 

    @Autowired
    private AgrupacionRepository agrupacionRepository; 

    @Autowired
    private ConcursoRepository concursoRepository;
    
    @Autowired
    private ConcursoService concursoService;

    // 1. Obtener inscripciones de un representante
    @GetMapping("/representante/{idRepresentante}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<Inscripcion>> getInscripcionesRepresentante(@PathVariable Integer idRepresentante) {
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
    @SuppressWarnings("unchecked")
    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> crearInscripcion(@RequestBody java.util.Map<String, Object> payload) {
        try {
            Inscripcion nuevaInscripcion = new Inscripcion();
            
            // 1. VINCULAR Y CARGAR EL CONCURSO REAL DE LA BD
            es.uma.ajdp.tfg.elpapelillo.models.Concurso concursoReal = null;
            if (payload.containsKey("concurso") && payload.get("concurso") != null) {
                java.util.Map<String, Object> concursoMap = (java.util.Map<String, Object>) payload.get("concurso");
                if (concursoMap.containsKey("idConcurso") && concursoMap.get("idConcurso") != null) {
                    int idConcurso = ((Number) concursoMap.get("idConcurso")).intValue();
                    
                    concursoReal = concursoRepository.findById(idConcurso).orElse(null);
                    if (concursoReal != null) {
                        nuevaInscripcion.setConcurso(concursoReal);
                    }
                }
            }
            
            if (concursoReal == null) {
                return ResponseEntity.badRequest().body("El concurso especificado no existe o es obligatorio.");
            }
            
            // EXTRAER EL AÑO DE LA FECHA
            Integer anioConcurso = null;
            if (concursoReal.getFechaInicio() != null) {
                Object fechaObj = concursoReal.getFechaInicio();
                
                if (fechaObj instanceof java.util.Date) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime((java.util.Date) fechaObj);
                    anioConcurso = cal.get(java.util.Calendar.YEAR);
                } else if (fechaObj instanceof java.time.temporal.TemporalAccessor) {
                    anioConcurso = ((java.time.temporal.TemporalAccessor) fechaObj).get(java.time.temporal.ChronoField.YEAR);
                }
            }
            
            // 2. CONSTRUIR Y MAPEAR LA AGRUPACIÓN
            if (payload.containsKey("agrupacion") && payload.get("agrupacion") != null) {
                java.util.Map<String, Object> agrupacionMap = (java.util.Map<String, Object>) payload.get("agrupacion");
                
                es.uma.ajdp.tfg.elpapelillo.models.Agrupacion agrupacionFinal = null;

                String tipoStr = (String) agrupacionMap.get("tipo"); 
                if (tipoStr == null) {
                    tipoStr = (String) agrupacionMap.get("tipoConcurso");
                }

                String nombreAgrupacion = (String) agrupacionMap.get("nombre");

                // VALIDACIÓN: Nombre único por concurso vía Repositorio
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
                    // REUTILIZAR EXISTENTE
                    agrupacionFinal = new es.uma.ajdp.tfg.elpapelillo.models.Agrupacion() {};
                    agrupacionFinal.setIdAgrupacion(((Number) agrupacionMap.get("idAgrupacion")).intValue());
                    
                } else {
                    // CREAR NUEVA AGRUPACIÓN DESDE CERO
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
                            
                            String modStr = (String) camposEspecificosMap.get("modalidad");
                            if (modStr == null) {
                                modStr = (String) payload.get("modalidad");
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
                            
                            String modDiosStr = (String) camposEspecificosMap.get("categoria"); 
                            if (modDiosStr == null) {
                                modDiosStr = (String) payload.get("modalidadDios"); 
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
                        es.uma.ajdp.tfg.elpapelillo.models.AgrupacionOtros otros = new es.uma.ajdp.tfg.elpapelillo.models.AgrupacionOtros();
                        if (agrupacionMap.containsKey("agrupacionOtros")) {
                            camposEspecificosMap = (java.util.Map<String, Object>) agrupacionMap.get("agrupacionOtros");
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
                    
                    if (anioConcurso != null) {
                        agrupacionFinal.setAnio(anioConcurso);
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
                            agrupacionFinal.setRepresentante(rep);
                        }
                    }

                    // Mapear Categoría
                    if (agrupacionMap.get("categoria") != null) {
                        String catStr = (String) agrupacionMap.get("categoria");
                        try {
                            agrupacionFinal.setCategoria(es.uma.ajdp.tfg.elpapelillo.models.enums.CategoriaAgrupacion.valueOf(catStr.toUpperCase()));
                        } catch (Exception e) {
                            // Ignorar si choca con valores temporales
                        }
                    }

                    if (esNuevaAgrupacion) {
                        agrupacionFinal = agrupacionRepository.save(agrupacionFinal);
                    }

                    nuevaInscripcion.setAgrupacion(agrupacionFinal);
                }
            }
            
            // 3. PERSISTIR LA INSCRIPCIÓN FINAL
            Inscripcion guardada = inscripcionService.crearInscripcion(nuevaInscripcion);
            return ResponseEntity.ok(guardada);
            
        } catch (Exception e) {
            System.err.println("Error crítico en InscripcionController:");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
public ResponseEntity<?> getInscripcionPorId(
        @PathVariable Integer id,
        @RequestParam(value = "idUsuarioActual", required = true) Integer idUsuarioActual) {

    Optional<Inscripcion> inscripOpt = inscripcionRepository.findById(id);
    if (inscripOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\": \"Inscripción no encontrada.\"}");
    }

    Inscripcion inscripcion = inscripOpt.get();

    // 1. Comprobamos si el usuario actual es Administrador
    boolean esAdmin = usuarioRepository.findById(idUsuarioActual)
            .map(user -> user instanceof Administrador) // Modifica según cómo tengas tu herencia o roles
            .orElse(false);

    // 2. Si no es administrador, verificamos si es el Representante de la agrupación
    if (!esAdmin) {
        if (inscripcion.getAgrupacion() != null && inscripcion.getAgrupacion().getRepresentante() != null) {
            Integer idRepresentanteReal = inscripcion.getAgrupacion().getRepresentante().getIdUsuario();
            
            // Si el ID del usuario actual no coincide con el del dueño, entonces SÍ es un intruso
            if (!idUsuarioActual.equals(idRepresentanteReal)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"error\": \"Acceso denegado: No tienes permisos para ver esta inscripción.\"}");
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Si es Administrador o el Representante correcto, el flujo continúa:
    return ResponseEntity.ok(inscripcion);
}

    // 5. 🔒 Actualizar estado de inscripción (CON CERROJO IDOR)
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoInscripcion(
            @PathVariable Integer id, 
            @RequestParam(value = "idUsuarioActual", required = false) Integer idUsuarioActual,
            @RequestBody Map<String, String> body) {
        
        Inscripcion inscripcion = inscripcionService.obtenerInscripcionPorId(id);
        if (inscripcion == null) {
            return ResponseEntity.notFound().build();
        }

        // Bloqueamos cambios ilícitos de estado en grupos de otra organización
        if (idUsuarioActual != null && inscripcion.getConcurso() != null) {
            try {
                List<Concurso> concursosPermitidos = concursoService.listarConcursosSegunRol(idUsuarioActual);
                boolean esPermitido = concursosPermitidos.stream()
                        .anyMatch(c -> c.getIdConcurso().equals(inscripcion.getConcurso().getIdConcurso()));
                
                if (!esPermitido) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"message\": \"No tienes permisos para modificar agrupaciones ajenas.\"}");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        String nuevoEstado = body.get("estado");
        Inscripcion actualizada = inscripcionService.cambiarEstadoInscripcion(id, nuevoEstado);
        return ResponseEntity.ok(actualizada);
    }

    // 6. 🔒 Exportación individual de PDF (CON CERROJO IDOR)
    @GetMapping("/{id}/exportar-pdf")
    public ResponseEntity<byte[]> exportarListadoComponentesPdf(
            @PathVariable Integer id,
            @RequestParam(value = "idUsuarioActual", required = false) Integer idUsuarioActual) {
        try {
            Inscripcion inscripcion = inscripcionService.obtenerInscripcionPorId(id);
            if (inscripcion == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Si intentan forzar la descarga de otra organización, se corta la petición
            if (idUsuarioActual != null && inscripcion.getConcurso() != null) {
                List<Concurso> concursosPermitidos = concursoService.listarConcursosSegunRol(idUsuarioActual);
                boolean esPermitido = concursosPermitidos.stream()
                        .anyMatch(c -> c.getIdConcurso().equals(inscripcion.getConcurso().getIdConcurso()));
                
                if (!esPermitido) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            
            byte[] pdfBytes = inscripcionService.generarPdfComponentes(inscripcion);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            
            String nombreAgrupacion = "Agrupacion";
            if (inscripcion.getAgrupacion() != null && inscripcion.getAgrupacion().getNombre() != null) {
                nombreAgrupacion = inscripcion.getAgrupacion().getNombre().replace(" ", "_");
            }
            
            String nombreArchivo = "Listado_" + nombreAgrupacion + ".pdf";
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("Error en el controlador al exportar PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 7. Exportar el Listado Resumen General de todo el Concurso
    @PostMapping("/exportar-pdf-general")
    public ResponseEntity<byte[]> descargarPdfGeneral(
            @RequestParam("idConcurso") Long idConcurso,
            @RequestParam("nombreConcurso") String nombreConcurso) {
        try {
            List<Inscripcion> inscripciones = inscripcionService.obtenerInscripcionesPorConcurso(idConcurso.intValue()); 

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

    // 8. Exportar fichas de los componentes de grupos SELECCIONADOS (Checkbox)
    @PostMapping("/exportar-pdf-seleccionados")
    public ResponseEntity<byte[]> exportarPdfSeleccionados(@RequestBody List<Integer> idsInscripcionesInt) {
        try {
            if (idsInscripcionesInt == null || idsInscripcionesInt.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

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