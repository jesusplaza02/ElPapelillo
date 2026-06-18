package es.uma.ajdp.tfg.elpapelillo.models;

public class LoginResponse {
    private String token;
    private String rol;
    private String email;
    private Integer idUsuario;
    private Integer id_organizacion; 

    public LoginResponse(String token, String rol, String email, Integer idUsuario, Integer id_organizacion) {
        this.token = token;
        this.rol = rol;
        this.email = email;
        this.idUsuario = idUsuario;
        this.id_organizacion = id_organizacion;
    }

    // Getters necesarios para el JSON
    public String getToken() { return token; }
    public String getRol() { return rol; }
    public String getEmail() { return email; }
    public Integer getIdUsuario() { return idUsuario; }
    public Integer getId_organizacion() { return id_organizacion; } 
}