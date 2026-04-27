package es.uma.ajdp.tfg.elpapelillo.services;

import es.uma.ajdp.tfg.elpapelillo.models.Organizacion;
import es.uma.ajdp.tfg.elpapelillo.repositories.OrganizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrganizacionService {
    @Autowired
    private OrganizacionRepository repository;

    public Organizacion guardar(@NonNull Organizacion org) {
        return repository.save(org);
    }

    public List<Organizacion> listarTodas() {
        return repository.findAll();
    }
}