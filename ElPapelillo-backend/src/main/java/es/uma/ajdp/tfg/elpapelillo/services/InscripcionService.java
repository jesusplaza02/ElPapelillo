package es.uma.ajdp.tfg.elpapelillo.services;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPTable; 
import com.lowagie.text.pdf.PdfPCell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.uma.ajdp.tfg.elpapelillo.models.Inscripcion;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.repositories.InscripcionRepository;

import com.lowagie.text.Font;
import java.awt.Color;

@Service
public class InscripcionService {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    public List<Inscripcion> obtenerInscripcionesPorRepresentante(Integer idRepresentante) {
        return inscripcionRepository.findByAgrupacion_Representante_IdUsuario(idRepresentante);
    }

    // 2. Crear una nueva inscripción con la lógica de negocio aplicada
    public Inscripcion crearInscripcion(Inscripcion nuevaInscripcion) {
        nuevaInscripcion.setFechaInscripcion(LocalDateTime.now());
        nuevaInscripcion.setEstadoInscripcion(EstadoAdministrativo.PENDIENTE);
        
        return inscripcionRepository.save(nuevaInscripcion);
    }

    // --- NUEVO MÉTODO PARA CARGAR LOS DATOS EN EL DETALLE DEL CONCURSO ---
    public List<Inscripcion> obtenerInscripcionesPorConcurso(Integer idConcurso) {
        // Llama al repositorio usando la relación que tiene Inscripcion con Concurso
        return inscripcionRepository.findByConcursoIdConcurso(idConcurso);
    }

    // ===================================================================
    // NUEVO: Obtener una inscripción por su ID (Elimina el primer error)
    // ===================================================================
    public Inscripcion obtenerInscripcionPorId(Integer id) {
        return inscripcionRepository.findById(id).orElse(null);
    }

    // ===================================================================
    // NUEVO: Modificar el estado transformando String a Enum (Elimina el segundo error)
    // ===================================================================
    public Inscripcion cambiarEstadoInscripcion(Integer id, String nuevoEstadoStr) {
        // 1. Buscamos la inscripción en la base de datos
        Inscripcion ins = inscripcionRepository.findById(id).orElse(null);
        
        if (ins != null && nuevoEstadoStr != null) {
            try {
                // 2. Convertimos el String ("APROBADO" / "RECHAZADO") que viene del Front en tu Enum de Java
                EstadoAdministrativo nuevoEstadoEnum = EstadoAdministrativo.valueOf(nuevoEstadoStr.toUpperCase());
                
                // 3. Asignamos el estado mapeado y guardamos en la BD
                ins.setEstadoInscripcion(nuevoEstadoEnum);
                return inscripcionRepository.save(ins);
                
            } catch (IllegalArgumentException e) {
                System.err.println("El estado enviado '" + nuevoEstadoStr + "' no coincide con ningún valor del Enum EstadoAdministrativo.");
            }
        }
        return null;
    }

    // ===================================================================
    // >>> MÉTODOS AÑADIDOS PARA COMPATIBILIDAD CON TU CONTROLLER <<<
    // ===================================================================

    /**
     * Sobrecarga que recibe Long desde el Controller, lo convierte de forma 
     * segura a Integer y reutiliza tu método original sin alterar tu lógica.
     */
    public Inscripcion obtenerInscripcionPorId(Long id) {
        if (id == null) return null;
        return this.obtenerInscripcionPorId(id.intValue());
    }

    public byte[] generarPdfComponentes(Inscripcion inscripcion) {
    if (inscripcion == null) {
        return new byte[0];
    }
    
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        // 1. Configurar documento A4 con márgenes proporcionales
        Document documento = new Document(com.lowagie.text.PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(documento, baos);
        
        documento.open();
        
        // 2. Paleta de colores y fuentes estilo oficial de la plataforma
        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(30, 70, 32));
        Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 100, 100));
        Font fuenteSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(43, 138, 62));
        Font fuenteTextoNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(33, 37, 41));
        Font fuenteTextoNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(73, 80, 87));
        Font fuenteFilaComponente = FontFactory.getFont(FontFactory.COURIER, 9, new Color(33, 37, 41));
        
        LineSeparator lineaDivisoria = new LineSeparator(1f, 100f, new Color(222, 226, 230), Element.ALIGN_CENTER, -2);
        LineSeparator lineaVerde = new LineSeparator(2f, 100f, new Color(43, 138, 62), Element.ALIGN_CENTER, -2);

        // 3. Extracción segura de la información de Hibernate
        String nombreAgrupacion = "Sin Agrupacion";
        String nombreConcurso = "Sin Concurso";
        
        if (inscripcion.getAgrupacion() != null && inscripcion.getAgrupacion().getNombre() != null) {
            nombreAgrupacion = inscripcion.getAgrupacion().getNombre();
        }
        if (inscripcion.getConcurso() != null && inscripcion.getConcurso().getNombre() != null) {
            nombreConcurso = inscripcion.getConcurso().getNombre();
        }

        // ===================================================================
        // 4. CABECERA ESTRUCTURAL (Tabla de 2 columnas para Título y Logo)
        // ===================================================================
        PdfPTable tablaCabecera = new PdfPTable(2);
        tablaCabecera.setWidthPercentage(100);
        tablaCabecera.setWidths(new float[]{75f, 25f}); // 75% para texto, 25% para el logo
        
        // Celda Izquierda: Bloque de Títulos
        PdfPCell celdaIzquierda = new PdfPCell();
        celdaIzquierda.setBorder(PdfPCell.NO_BORDER);
        celdaIzquierda.setVerticalAlignment(Element.ALIGN_BOTTOM); // Alinea el texto abajo para cuadrar con el logo
        
        Paragraph pTitulo = new Paragraph("LISTADO OFICIAL DE PARTICIPANTES", fuenteTitulo);
        pTitulo.setSpacingAfter(4);
        celdaIzquierda.addElement(pTitulo);
        
        Paragraph pSubtitulo = new Paragraph("ElPapelillo app • CONCURSO: " + nombreConcurso.toUpperCase(), fuenteSubtitulo);
        celdaIzquierda.addElement(pSubtitulo);
        
        tablaCabecera.addCell(celdaIzquierda);
        
        // Celda Derecha: Contenedor del Logo alineado arriba a la derecha
        PdfPCell celdaDerecha = new PdfPCell();
        celdaDerecha.setBorder(PdfPCell.NO_BORDER);
        celdaDerecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaDerecha.setVerticalAlignment(Element.ALIGN_TOP);
        
        try {
            String rutaLogo = "src/main/resources/static/images/Logo-ElPapelillo.png"; 
            Image logo = Image.getInstance(rutaLogo);
            logo.scaleToFit(65, 65); // Escalado elegante para que no se coma la cabecera
            logo.setAlignment(Element.ALIGN_RIGHT);
            celdaDerecha.addElement(logo);
        } catch (Exception e) {
            System.out.println("Nota: Omitiendo el recurso gráfico del logo al no encontrarse en el path estático.");
        }
        
        tablaCabecera.addCell(celdaDerecha);
        tablaCabecera.setSpacingAfter(10); // Margen inferior antes de pintar la línea verde
        
        // Inyectamos la cabecera ya cuadrada en el documento
        documento.add(tablaCabecera);
        
        // Línea verde superior justo por debajo de títulos y logotipo
        documento.add(lineaVerde);
        documento.add(new Paragraph(" "));

        // 5. BLOQUE INFORMATIVO DE LA AGRUPACIÓN
        Paragraph pSecDatos = new Paragraph("DATOS DE LA AGRUPACIÓN", fuenteSeccion);
        pSecDatos.setSpacingAfter(6);
        documento.add(pSecDatos);
        
        String estado = (inscripcion.getEstadoInscripcion() != null) ? inscripcion.getEstadoInscripcion().toString() : "PENDIENTE";
        
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String fechaImpresionConHora = ahora.format(formateador);
        
        Paragraph pDatos = new Paragraph();
        pDatos.setFont(fuenteTextoNormal);
        pDatos.add(new com.lowagie.text.Chunk("Nombre Agrupación: ", fuenteTextoNegrita));
        pDatos.add(nombreAgrupacion + "\n");
        pDatos.add(new com.lowagie.text.Chunk("Estado Administrativo: ", fuenteTextoNegrita));
        pDatos.add(estado + "\n");
        pDatos.add(new com.lowagie.text.Chunk("Fecha de Impresión: ", fuenteTextoNegrita));
        pDatos.add(fechaImpresionConHora);
        pDatos.setSpacingAfter(15);
        documento.add(pDatos);

        documento.add(lineaDivisoria);
        documento.add(new Paragraph(" "));

        // 6. BLOQUE DEL LISTADO DE COMPONENTES
        Paragraph pSecComp = new Paragraph("LISTA DE PARTICIPANTES", fuenteSeccion);
        pSecComp.setSpacingAfter(10);
        documento.add(pSecComp);
        
        if (inscripcion.getParticipaciones() != null && !inscripcion.getParticipaciones().isEmpty()) {
            
            Paragraph cabeceraLista = new Paragraph("   NOMBRE COMPLETO           | DNI/NIE    | F. NACIMIENTO | ROL/INSTRUMENTO", fuenteTextoNegrita);
            cabeceraLista.setSpacingAfter(6);
            documento.add(cabeceraLista);
            documento.add(lineaDivisoria);
            documento.add(new Paragraph(" "));

            for (es.uma.ajdp.tfg.elpapelillo.models.Participacion p : inscripcion.getParticipaciones()) {
                var participante = p.getParticipante();
                
                if (participante != null) {
                    String nombreComp = participante.getNombre() != null ? participante.getNombre() : "Sin nombre";
                    String dniComp = participante.getDni() != null ? participante.getDni() : "--------";
                    
                    String fechaNacComp = "----------";
                    if (participante.getFechaNacimiento() != null) {
                        fechaNacComp = participante.getFechaNacimiento().toString();
                    }
                    
                    String rolComp = p.getRol() != null ? p.getRol().toString() : "COMPONENTE";

                    String filaFormateada = String.format(" - %-25s | %-10s | %-13s | %s", 
                        recortarTexto(nombreComp, 25), 
                        dniComp, 
                        fechaNacComp,
                        rolComp);
                    
                    Paragraph pFila = new Paragraph(filaFormateada, fuenteFilaComponente);
                    pFila.setSpacingAfter(4);
                    documento.add(pFila);
                }
            }
        } else {
            Paragraph pVacio = new Paragraph("No se han encontrado componentes asignados a esta inscripción.", fuenteTextoNormal);
            documento.add(pVacio);
        }
        
        // 7. PIE DE PÁGINA ASOCIADO AL CONCURSO
        documento.add(new Paragraph(" "));
        documento.add(lineaDivisoria);
        
        String textoPieModificado = "Este documento sirve como justificante oficial de la composición de la agrupación artística ante la comisión organizadora del concurso " + nombreConcurso + ".";
        
        Paragraph pPie = new Paragraph(textoPieModificado, fuenteSubtitulo);
        pPie.setAlignment(Element.ALIGN_CENTER);
        pPie.setSpacingBefore(15);
        documento.add(pPie);

        documento.close();
        return baos.toByteArray();
        
    } catch (Exception e) {
        System.err.println("Error al procesar el diseño visual adaptado: " + e.getMessage());
        return new byte[0];
    }
}

private String recortarTexto(String texto, int max) {
    if (texto.length() > max) {
        return texto.substring(0, max - 3) + "...";
    }
    return texto;
}

    
}