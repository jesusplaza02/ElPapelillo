package es.uma.ajdp.tfg.elpapelillo.models;

public class LoginResponse {
    private String token;
    private String rol;
    private String email;
    private Integer idUsuario; // 1. Terminamos de declararlo

    // 2. Actualizamos el constructor para que acepte el ID
    public LoginResponse(String token, String rol, String email, Integer idUsuario) {
        this.token = token;
        this.rol = rol;
        this.email = email;
        this.idUsuario = idUsuario;
    }

    // 3. Añadimos el Getter para que Jackson (el serializador) pueda leerlo
    public String getToken() { return token; }
    public String getRol() { return rol; }
    public String getEmail() { return email; }
    public Integer getIdUsuario() { return idUsuario; } // ¡Fundamental!
}