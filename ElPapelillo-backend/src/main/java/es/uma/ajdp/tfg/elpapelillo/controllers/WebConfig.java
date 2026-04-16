package es.uma.ajdp.tfg.elpapelillo.controllers;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/archivos/**")
                .addResourceLocations("file:archivos/");
    }

    // AÑADE ESTO AQUÍ DEBAJO
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/archivos/**") // Permite CORS en la ruta de archivos
                .allowedOrigins("http://localhost:4200") // Tu URL de Angular
                .allowedMethods("GET")
                .exposedHeaders("Content-Disposition"); // Importante para el nombre del archivo
    }
}