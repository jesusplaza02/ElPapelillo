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
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("La inscripción con ID " + idInscripcion + " no existe."));

        String nombreOriginal = archivo.getOriginalFilename();
        String nombreLimpio = System.currentTimeMillis() + "_" + 
                (nombreOriginal != null ? nombreOriginal.replaceAll("\\s+", "_") : "fianza.pdf");
        
        Path directorio = Paths.get("archivos/fianzas");
        if (!Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }
        
        Path rutaCompleta = directorio.resolve(nombreLimpio);
        Files.copy(archivo.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);

        Fianza fianza = inscripcion.getFianza();
        if (fianza == null) {
            fianza = new Fianza();
        }

        fianza.setImporte(importe); 
        fianza.setFechaPago(fechaPago != null ? fechaPago : LocalDateTime.now());
        fianza.setRutaRecibo("archivos/fianzas/" + nombreLimpio);
        fianza.setPagada(true); 

 
        Fianza fianzaGuardada = fianzaRepository.save(fianza);

        inscripcion.setFianza(fianzaGuardada);
        inscripcionRepository.save(inscripcion);

        return fianzaGuardada;
    }

    @Transactional
    public void eliminarFianzaPorInscripcion(Integer idInscripcion) {
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("La inscripción con ID " + idInscripcion + " no existe."));

        Fianza fianza = inscripcion.getFianza();
        if (fianza != null) {
            
            inscripcion.setFianza(null);
            inscripcionRepository.save(inscripcion);
            
            if (fianza.getRutaRecibo() != null) {
                try {
                    Path rutaArchivo = Paths.get(fianza.getRutaRecibo());
                    Files.deleteIfExists(rutaArchivo);
                    System.out.println("Archivo físico de fianza eliminado con éxito: " + fianza.getRutaRecibo());
                } catch (IOException e) {
                    System.err.println("No se pudo eliminar el archivo físico del disco: " + e.getMessage());
                }
            }
            fianzaRepository.delete(fianza);
        }
    }
}