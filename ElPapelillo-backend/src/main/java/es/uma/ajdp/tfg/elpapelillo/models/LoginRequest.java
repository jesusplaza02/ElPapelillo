package es.uma.ajdp.tfg.elpapelillo.models;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String email;
    private String password;
}