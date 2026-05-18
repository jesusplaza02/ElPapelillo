package es.uma.ajdp.tfg.elpapelillo.services;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;

import es.uma.ajdp.tfg.elpapelillo.models.*;
import es.uma.ajdp.tfg.elpapelillo.repositories.*;

@Service
public class FianzaService {

    @Autowired
    private FianzaRepository fianzaRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

   @Transactional
    public Fianza subirseFianza(Integer idInscripcion, MultipartFile archivo, Double importe, LocalDateTime fechaPago) throws IOException {
        // 1. Verificar si la inscripción existe
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("La inscripción con ID " + idInscripcion + " no existe."));

        // 2. Guardar el archivo PDF físicamente en el disco
        String nombreOriginal = archivo.getOriginalFilename();
        String nombreLimpio = System.currentTimeMillis() + "_" + 
                (nombreOriginal != null ? nombreOriginal.replaceAll("\\s+", "_") : "fianza.pdf");
        
        Path directorio = Paths.get("archivos/fianzas");
        if (!Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }
        
        Path rutaCompleta = directorio.resolve(nombreLimpio);
        Files.copy(archivo.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);

        // 3. Obtener o crear el registro de Fianza
        Fianza fianza = inscripcion.getFianza();
        if (fianza == null) {
            fianza = new Fianza();
        }

        // MODIFICADO: Ahora seteamos los valores reales que vienen desde el formulario web
        fianza.setImporte(importe); 
        fianza.setFechaPago(fechaPago != null ? fechaPago : LocalDateTime.now()); // Si no viene fecha, pone la actual por seguridad
        fianza.setRutaRecibo("archivos/fianzas/" + nombreLimpio);
        fianza.setPagada(true); // Se marca como pagada al subir el comprobante

        // 4. Guardar fianza en la base de datos
        Fianza fianzaGuardada = fianzaRepository.save(fianza);

        // 5. Vincular la fianza a la inscripción y actualizar id_fianza en MySQL
        inscripcion.setFianza(fianzaGuardada);
        inscripcionRepository.save(inscripcion);

        return fianzaGuardada;
    }

    @Transactional
    public void eliminarFianzaPorInscripcion(Integer idInscripcion) {
        // 1. Buscar la inscripción afectada
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("La inscripción con ID " + idInscripcion + " no existe."));

        // 2. Obtener la fianza asociada
        Fianza fianza = inscripcion.getFianza();
        if (fianza != null) {
            
            // 3. Romper la relación en la tabla inscripcion (pone id_fianza = NULL en MySQL)
            inscripcion.setFianza(null);
            inscripcionRepository.save(inscripcion);
            
            // 4. Intentar borrar el archivo PDF físico guardado en el disco
            if (fianza.getRutaRecibo() != null) {
                try {
                    Path rutaArchivo = Paths.get(fianza.getRutaRecibo());
                    Files.deleteIfExists(rutaArchivo);
                    System.out.println("Archivo físico de fianza eliminado con éxito: " + fianza.getRutaRecibo());
                } catch (IOException e) {
                    System.err.println("No se pudo eliminar el archivo físico del disco: " + e.getMessage());
                    // No lanzamos excepción aquí para que el flujo de base de datos continúe si el archivo ya no existía
                }
            }

            // 5. Eliminar el registro definitivo de la tabla fianza
            fianzaRepository.delete(fianza);
        }
    }
}