package es.uma.ajdp.tfg.elpapelillo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uma.ajdp.tfg.elpapelillo.repositories.ParticipanteRepository;

@RestController
@RequestMapping("/api/participantes")
@CrossOrigin(origins = "http://localhost:4200")
public class ParticipanteController {

    @Autowired
    private ParticipanteRepository participanteRepository;

    @GetMapping("/buscar-dni/{dni}")
    public ResponseEntity<?> buscarPorDni(@PathVariable String dni) {
        return participanteRepository.findByDni(dni)
                .map(ResponseEntity::ok) // Si existe, devuelve el Participante con su ID 1
                .orElse(ResponseEntity.notFound().build()); // Si no existe, devuelve 404 para que el front sepa que debe crearlo desde cero
    }
}