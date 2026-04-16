package es.uma.ajdp.tfg.elpapelillo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.uma.ajdp.tfg.elpapelillo.models.Documento;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.repositories.AgrupacionRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.DocumentoRepository;
import jakarta.transaction.Transactional;

@Service
public class DocumentoService {

    @Autowired
    private DocumentoRepository documentoRepository;
    
    @Autowired
    private AgrupacionRepository agrupacionRepository;

    @Transactional
    public Documento guardarDocumento(MultipartFile archivo, Long idAgrupacion, String nombreDoc) {
        // 1. Simulación de guardado de archivo físico (aquí usarías File.copy o similar)
        String rutaSimulada = "archivos/" + archivo.getOriginalFilename();

        // 2. Crear el objeto Documento
        Documento doc = new Documento();
        doc.setNombre(nombreDoc);
        doc.setUrlArchivo(rutaSimulada);
        doc.setEstado(EstadoAdministrativo.PENDIENTE);

        // 3. Vincular con la Agrupación
       // Añadimos .intValue() para convertir el Long que viene del DTO al Integer que usa el Repo
        agrupacionRepository.findById(idAgrupacion.intValue())
            .ifPresent(doc::setAgrupacion   );

        // 4. Guardar en la base de datos
        return documentoRepository.save(doc);
    }

    public List<Documento> listarPorAgrupacion(Long idAgrupacion) {
        // Usamos el mismo truco del .intValue() si tu repositorio usa Integer
        return documentoRepository.findByAgrupacionIdAgrupacion(idAgrupacion.intValue());
    }
}