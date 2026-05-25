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
        String rutaSimulada = "archivos/" + archivo.getOriginalFilename();

        Documento doc = new Documento();
        doc.setNombre(nombreDoc);
        doc.setUrlArchivo(rutaSimulada);
        doc.setEstado(EstadoAdministrativo.PENDIENTE);

        inscripcionRepository.findById(idInscripcion)
            .ifPresent(doc::setInscripcion);

        return documentoRepository.save(doc);
    }

    public List<Documento> listarPorInscripcion(Integer idInscripcion) {
        return documentoRepository.findByInscripcionIdInscripcion(idInscripcion);
    }
}