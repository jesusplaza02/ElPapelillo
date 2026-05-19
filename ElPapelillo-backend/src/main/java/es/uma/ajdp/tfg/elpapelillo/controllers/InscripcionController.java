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
import es.uma.ajdp.tfg.elpapelillo.repositories.InscripcionRepository;
import es.uma.ajdp.tfg.elpapelillo.services.InscripcionService;


@RestController
@RequestMapping("/api/inscripciones")
@CrossOrigin(origins = "http://localhost:4200") // Evita problemas de CORS con Angular
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private InscripcionRepository inscripcionRepository; 


    // 1. Obtener inscripciones de un representante
    @GetMapping("/representante/{idRepresentante}")
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
    @PostMapping
    public ResponseEntity<Inscripcion> crearInscripcion(@RequestBody Inscripcion nuevaInscripcion) {
        Inscripcion guardada = inscripcionService.crearInscripcion(nuevaInscripcion);
        return ResponseEntity.ok(guardada);
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