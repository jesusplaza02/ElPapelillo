package es.uma.ajdp.tfg.elpapelillo.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import es.uma.ajdp.tfg.elpapelillo.models.Documento;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.repositories.InscripcionRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.DocumentoRepository;
import jakarta.transaction.Transactional;

@Service
public class DocumentoService {

    @Autowired
    private DocumentoRepository documentoRepository;
    
    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Transactional
    public Documento guardarDocumento(MultipartFile archivo, @NonNull Integer idInscripcion, String nombreDoc) {
        // 1. Simulación de guardado
        String rutaSimulada = "archivos/" + archivo.getOriginalFilename();

        // 2. Crear el objeto Documento
        Documento doc = new Documento();
        doc.setNombre(nombreDoc);
        doc.setUrlArchivo(rutaSimulada);
        doc.setEstado(EstadoAdministrativo.PENDIENTE);

        // 3. Vincular con la Inscripción (Usando Integer)
        // Arreglado: Eliminado el uso de Long para evitar errores de compilación
        inscripcionRepository.findById(idInscripcion)
            .ifPresent(doc::setInscripcion);

        // 4. Guardar
        return documentoRepository.save(doc);
    }

    public List<Documento> listarPorInscripcion(Integer idInscripcion) {
        // Coincide con el tipo Integer definido en el repositorio
        return documentoRepository.findByInscripcionIdInscripcion(idInscripcion);
    }
}