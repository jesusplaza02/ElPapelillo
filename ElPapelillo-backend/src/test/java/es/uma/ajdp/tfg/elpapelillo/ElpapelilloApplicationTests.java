package es.uma.ajdp.tfg.elpapelillo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ElPapelilloApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("TC-01: Comprobación del ciclo de vida del backend aislado de forma transaccional")
    void contextLoads() {
    }

    @Test
    @DisplayName("TC-02: Administrador inicia sesión correctamente")
    void test_TC02_loginAdministradorExitoso() throws Exception {
        String loginPayload = "{\"email\":\"pepe@gmail.com\",\"password\":\"iROrrV60wj\"}";

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200) {
                        throw new AssertionError("Se esperaba estado 200, pero se obtuvo: " + status);
                    }
                });
    }

    @Test
    @DisplayName("TC-03: Administrador intenta iniciar sesión con contraseña incorrecta")
    void test_TC03_loginAdministradorContraseniaIncorrecta() throws Exception {
        String loginPayload = "{\"email\":\"pepe@gmail.com\",\"password\":\"contraseñadementira\"}";
        
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 401) {
                        throw new AssertionError("El sistema no devolvió 401 ante una contraseña incorrecta. Estado: " + status);
                    }
                });
    }

    @Test
    @DisplayName("TC-04: Login de usuario con contraseña inválida")
    void test_TC04_loginCredencialesInvalidas() throws Exception {
        String loginPayload = "{\"email\":\"antoniojesusdiazplaza@gmail.com\",\"password\":\"erronea\"}";
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().is4xxClientError()); 
    }

    @Test
    @DisplayName("TC-05: Representante inscribe nueva agrupación de canto en concurso")
    void test_TC05_inscripcionNuevaAgrupacionCanto() throws Exception {
        String nombreUnico = "Agrupacion Real " + System.currentTimeMillis();
        String payload = "{"
                + "\"concurso\":{\"idConcurso\":1},"
                + "\"agrupacion\":{"
                    + "\"nombre\":\"" + nombreUnico + "\","
                    + "\"categoria\":\"ADULTO\","
                    + "\"tipoConcurso\":\"CANTO\","
                    + "\"modalidad\":\"COMPARSA\""
                + "}"
            + "}";

        mockMvc.perform(post("/api/inscripciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200) {
                        throw new AssertionError("Se esperaba estado 200, pero se obtuvo: " + status);
                    }
                    }); 
    }

    @Test
    @DisplayName("TC-06: Representante inscribe agrupación existente repetida en el mismo concurso")
    void test_TC06_inscripcionAgrupacionRepetida() throws Exception {
        String payload = "{"
                + "\"concurso\":{\"idConcurso\":1},"
                + "\"agrupacion\":{"
                    + "\"nombre\":\"El poeta majareta de La Malagueta\"," 
                    + "\"categoria\":\"ADULTO\","
                    + "\"tipoConcurso\":\"CANTO\""
                + "}"
            + "}";

        mockMvc.perform(post("/api/inscripciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().is4xxClientError()); 
    }

    @Test
    @DisplayName("TC-07: Representante inscribe nueva agrupación Drag en un concurso")
    void test_TC07_inscripcionNuevaAgrupacionDrag() throws Exception {
        String nombreUnicoDrag = "Drag Real " + System.currentTimeMillis();
        String payload = "{"
                + "\"concurso\":{\"idConcurso\":1},"
                + "\"agrupacion\":{"
                    + "\"nombre\":\"" + nombreUnicoDrag + "\","
                    + "\"tipoConcurso\":\"DRAG\","
                    + "\"nombreArtisticoDrag\":\"Drag Real Test\""
                + "}"
            + "}";

        mockMvc.perform(post("/api/inscripciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200) {
                        throw new AssertionError("Se esperaba estado 200, pero se obtuvo: " + status);
                    }
                }); 
    }

    @Test
    @DisplayName("TC-08: Representante filtra sus agrupaciones base o históricas")
    void test_TC08_filtrarAgrupacionesPorTipo() throws Exception {
        mockMvc.perform(get("/api/agrupaciones/base/14")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200) {
                        throw new AssertionError("Se esperaba estado 200, pero se obtuvo: " + status);
                    }
                });
    }

    @Test
    @DisplayName("TC-09: Representante sube un documento válido de inscripción")
    void test_TC09_subidaDocumentoValido() throws Exception {
        org.springframework.mock.web.MockMultipartFile archivoPdfSimulado = 
            new org.springframework.mock.web.MockMultipartFile(
                "file", "documento_inscripcion.pdf", "application/pdf", 
                "Contenido de prueba".getBytes()
            );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/documentos/upload")
                .file(archivoPdfSimulado)
                .param("idInscripcion", "1")       
                .param("nombreDoc", "DNI Revisor")
                .param("tipo", "PDF")
                .param("usuarioId", "14")) 
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200) {
                        throw new AssertionError("Se esperaba estado 200, pero se obtuvo: " + status);
                    }
                }); 
    }

    @Test
    @DisplayName("TC-10: Representante adjunta documento de más de 5MB")
    void test_TC10_subidaDocumentoExcedeTamaño() throws Exception {
        org.springframework.mock.web.MockMultipartFile archivoPesado = 
            new org.springframework.mock.web.MockMultipartFile(
                "file", "grande.pdf", "application/pdf", new byte[6 * 1024 * 1024] 
            );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/documentos/upload")
                .file(archivoPesado)
                .param("idInscripcion", "1")       
                .param("nombreDoc", "Fianza Invalida")
                .param("tipo", "PDF")
                .param("usuarioId", "14"))
                .andExpect(status().is4xxClientError()); 
    }

    @Test
@DisplayName("TC-11: Administrador rechaza documento de agrupación con mensaje")
void test_TC11_administradorRechazaDocumento() throws Exception {
    String payload = "{\"estado\":\"RECHAZADO\",\"comentarioRevision\":\"No se lee bien el reverso\"}";
    mockMvc.perform(put("/api/documentos/1")
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMINISTRADOR"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isOk()); 
}

@Test
@DisplayName("TC-12: Administrador aprueba documento de agrupación")
void test_TC12_administradorApruebaDocumento() throws Exception {
    String payload = "{\"estado\":\"APROBADO\",\"comentarioRevision\":\"Validado correctamente\"}";

    mockMvc.perform(put("/api/documentos/2")
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMINISTRADOR"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isOk()); 
}

    @Test
    @DisplayName("TC-13: Administrador descarga la lista de participantes de una agrupación en PDF")
    void test_TC13_descargarListadoParticipantesPdf() throws Exception {
        mockMvc.perform(get("/api/inscripciones/1/exportar-pdf")
                .param("idUsuarioActual", "1")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMINISTRADOR")))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    
                    if (status != 200) {
                        throw new AssertionError("Se esperaba estado 200 (OK) por base de datos vacía, pero se obtuvo: " + status);
                    }
                });
    }
    @Test
    @DisplayName("TC-14: Administrador aprueba la inscripción de una agrupación")
    void test_TC14_aprobarInscripcionAgrupacion() throws Exception {
        String estadoPayload = "{\"estado\":\"APROBADO\"}";

        mockMvc.perform(put("/api/inscripciones/1/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(estadoPayload))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200) {
                        throw new AssertionError("Se esperaba estado 200, pero se obtuvo: " + status);
                    }
                }); 
    }

    @Test
    @DisplayName("TC-15: Administrador adjunta fianza a agrupación de concurso activo")
    void test_TC15_administradorAdjuntaFianza() throws Exception {
        org.springframework.mock.web.MockMultipartFile file = 
            new org.springframework.mock.web.MockMultipartFile("file", "fianza.pdf", "application/pdf", "Pago".getBytes());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/fianzas/upload/1")
                .file(file)
                .param("importe", "300.0")
                .param("fechaPago", "2026-05-18T14:07:00"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200) {
                        throw new AssertionError("Se esperaba estado 200, pero se obtuvo: " + status);
                    }
                }); 
    }

    @Test
    @DisplayName("TC-16: Administrador intenta eliminar la fianza de una agrupación")
    void test_TC16_administradorEliminaFianza() throws Exception {
        mockMvc.perform(delete("/api/fianzas/inscripcion/1")) 
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200) {
                        throw new AssertionError("Se esperaba estado 200, pero se obtuvo: " + status);
                    }
                }); 
    }

    @Test
    @DisplayName("TC-17: Representante añade participante ya existente en esa agrupación")
    void test_TC17_insertarParticipanteRepetidoEnMismaAgrupacion() throws Exception {
        String payload = "{\"dni\":\"11223344A\",\"nombre\":\"Juan\"}";
        mockMvc.perform(post("/api/agrupaciones/1/participantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().is4xxClientError()); 
    }

    @Test
    @DisplayName("TC-18: Representante elimina a participante de inscripción en concurso activo")
    void test_TC18_eliminarParticipanteDeInscripcionActiva() throws Exception {
        mockMvc.perform(delete("/api/participantes/eliminar/1")
                .param("idUsuarioActual", "14"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 500) throw new AssertionError("Error interno en el servidor (500)");
                }); 
    }

    @Test
    @DisplayName("TC-19: Administrador envía correo electrónico circular a todos los representantes")
    void test_TC19_envioCorreoCircularConcurso() throws Exception {
        String idsInscripcionesJson = "[1,4]"; 

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/concursos/enviar-circular")
                .param("asunto", "Circular Urgente TFG")
                .param("cuerpo", "Contenido del correo masivo")
                .param("idsInscripciones", idsInscripcionesJson))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200) {
                        throw new AssertionError("Se esperaba estado 200, pero se obtuvo: " + status);
                    }
                }); 
    }

    @Test
    @DisplayName("TC-20: Superadmin registra una nueva organización")
    void test_TC20_registrarNuevaOrganizacion() throws Exception {
        String nombreUnico = "Federación de Carnaval " + System.currentTimeMillis();

        String organizacionPayload = "{"
                + "\"nombre\":\"" + nombreUnico + "\","
                + "\"email\":\"contacto@fedcarnaval.es\","
                + "\"telefono\":\"600123456\","
                + "\"ubicacion\":\"Málaga\","
                + "\"activo\":true"
                + "}";

        mockMvc.perform(post("/api/organizaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(organizacionPayload))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200) {
                        throw new AssertionError("Se esperaba estado 200, pero se obtuvo: " + status);
                    }
                });
    }

    @Test
    @DisplayName("TC-21: Representante añade participante ya existente en la BD (Importar datos)")
    void test_TC21_añadirParticipanteExistenteBaseDatos() throws Exception {
        mockMvc.perform(get("/api/participantes/buscar-historico")
                .param("dni", "22447719H")
                .param("idUsuarioActual", "14")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200) {
                        throw new AssertionError("Se esperaba estado 200, pero se obtuvo: " + status);
                    }
                }); 
    }
}