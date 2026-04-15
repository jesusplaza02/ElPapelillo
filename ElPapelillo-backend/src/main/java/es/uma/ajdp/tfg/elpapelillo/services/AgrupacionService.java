package es.uma.ajdp.tfg.elpapelillo.services;

import es.uma.ajdp.tfg.elpapelillo.models.Agrupacion;
import es.uma.ajdp.tfg.elpapelillo.repositories.AgrupacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AgrupacionService {

    @Autowired
    private AgrupacionRepository agrupacionRepository;

    public List<Agrupacion> findByRepresentanteId(Integer idRep) {
        return agrupacionRepository.findByRepresentanteId(idRep);
    }
}