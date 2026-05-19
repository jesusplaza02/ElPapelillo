package es.uma.ajdp.tfg.elpapelillo.services;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable; 
import com.lowagie.text.pdf.PdfPCell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.uma.ajdp.tfg.elpapelillo.models.Inscripcion;
import es.uma.ajdp.tfg.elpapelillo.models.Participacion;
import es.uma.ajdp.tfg.elpapelillo.models.Participante;
import es.uma.ajdp.tfg.elpapelillo.models.enums.EstadoAdministrativo;
import es.uma.ajdp.tfg.elpapelillo.repositories.InscripcionRepository;
import es.uma.ajdp.tfg.elpapelillo.repositories.ParticipanteRepository;

import com.lowagie.text.Font;
import java.awt.Color;

@Service
public class InscripcionService {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private ParticipanteRepository participanteRepository;

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

    // 2. Paleta de colores y fuentes estilo oficial de la plataforma
        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(30, 70, 32));
        Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 100, 100));
        Font fuenteSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(43, 138, 62));
        Font fuenteTextoNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(33, 37, 41));
        Font fuenteTextoNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(73, 80, 87));
        Font fuenteFilaComponente = FontFactory.getFont(FontFactory.COURIER, 9, new Color(33, 37, 41));
        
    public byte[] generarPdfComponentes(Inscripcion inscripcion) {
    if (inscripcion == null) {
        return new byte[0];
    } try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        // 1. Configurar documento A4 con márgenes proporcionales
        Document documento = new Document(com.lowagie.text.PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(documento, baos);
        
        documento.open();
        
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

    // ==========================================
    // PDF 1: LISTADO GENERAL DE AGRUPACIONES (Ajustado a 4 Columnas)
    // ==========================================
    public byte[] generarPdfGeneralConcurso(String nombreConcurso, List<Inscripcion> inscripciones) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(documento, baos);
            documento.open();

            // Cabecera estructural reutilizable
            documento.add(crearEstructuraCabecera("RESUMEN GENERAL DE INSCRIPCIONES", nombreConcurso));
            documento.add(new LineSeparator(2f, 100f, new Color(43, 138, 62), Element.ALIGN_CENTER, -2));
            documento.add(new Paragraph(" "));

            //
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            // 2. Obtenemos la fecha y hora actual del sistema
            String fechaHoraActual = LocalDateTime.now().format(formatter);

            // 3. Creamos el párrafo con el texto (puedes usar tu variable 'fuenteTextoNormal')
            Paragraph pFecha = new Paragraph("Fecha y hora de impresión: " + fechaHoraActual, fuenteTextoNormal);
            pFecha.setAlignment(Element.ALIGN_RIGHT); // Lo alineamos a la derecha para que quede elegante

            // 4. Lo añadimos al documento antes de las tablas
            documento.add(pFecha);
            documento.add(new Paragraph(" "));
            //

            // Tabla principal con 4 columnas
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{35f, 20f, 30f, 15f}); 

            // Estilos para la cabecera principal
            String[] headers = {"Nombre Agrupación", "Categoría", "Representante", "Estado"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, fuenteTextoNegrita));
                cell.setBackgroundColor(new Color(240, 244, 241));
                cell.setPadding(6);
                tabla.addCell(cell);
            }

            // Población de datos
            for (Inscripcion ins : inscripciones) {
                if (ins == null) continue;

                String nombreAgrup = (ins.getAgrupacion() != null && ins.getAgrupacion().getNombre() != null) ? ins.getAgrupacion().getNombre() : "Sin nombre";
                String categoria = (ins.getAgrupacion() != null && ins.getAgrupacion().getCategoria() != null) ? String.valueOf(ins.getAgrupacion().getCategoria()) : "Sin categoría";
                
                String rep = "Sin asignar";
                if (ins.getAgrupacion() != null && ins.getAgrupacion().getRepresentante() != null && ins.getAgrupacion().getRepresentante().getNombre() != null) {
                    rep = ins.getAgrupacion().getRepresentante().getNombre();
                }
                
                String estado = (ins.getEstadoInscripcion() != null) ? String.valueOf(ins.getEstadoInscripcion()) : "PENDIENTE";

                // Añadimos las 4 celdas de la agrupación
                Color colorFilaAgrup = new Color(250, 250, 250);
                PdfPCell c1 = new PdfPCell(new Paragraph(nombreAgrup, fuenteTextoNegrita)); 
                PdfPCell c2 = new PdfPCell(new Paragraph(categoria, fuenteTextoNormal));
                PdfPCell c3 = new PdfPCell(new Paragraph(rep, fuenteTextoNormal));
                PdfPCell c4 = new PdfPCell(new Paragraph(estado, fuenteTextoNormal));
                
                c1.setBackgroundColor(colorFilaAgrup); c1.setPadding(5);
                c2.setBackgroundColor(colorFilaAgrup); c2.setPadding(5);
                c3.setBackgroundColor(colorFilaAgrup); c3.setPadding(5);
                c4.setBackgroundColor(colorFilaAgrup); c4.setPadding(5);
                
                tabla.addCell(c1);
                tabla.addCell(c2);
                tabla.addCell(c3);
                tabla.addCell(c4);

                // --- NUEVA CORRECCIÓN: EXTRAER PARTICIPANTES DESDE LA INTERMEDIA ---
                // Creamos la celda contenedora para la subtabla
                PdfPCell celdaContenedorParticipantes = new PdfPCell();
                celdaContenedorParticipantes.setColspan(4);
                celdaContenedorParticipantes.setPaddingLeft(25); 
                celdaContenedorParticipantes.setPaddingRight(10);
                celdaContenedorParticipantes.setPaddingTop(5);
                celdaContenedorParticipantes.setPaddingBottom(10);
                celdaContenedorParticipantes.setBorderWidthTop(0); 

                // Subtabla para los integrantes (4 columnas: Nombre, DNI, Fecha de Nacimiento, Rol de la intermedia)
                PdfPTable subTablaParts = new PdfPTable(4);
                subTablaParts.setWidthPercentage(100);
                subTablaParts.setWidths(new float[]{50f, 20f, 25f, 25f});

                Font fuenteMiniNegrita = new Font(Font.HELVETICA, 8, Font.BOLD, Color.DARK_GRAY);
                Font fuenteMiniNormal = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);
                Color colorHeaderSub = new Color(238, 240, 242);

                String[] subHeaders = {"Nombre Participante", "DNI/NIE", "Fecha de Nacimiento", "Rol / Instrumento"};
                for (String sh : subHeaders) {
                    PdfPCell sc = new PdfPCell(new Paragraph(sh, fuenteMiniNegrita));
                    sc.setBackgroundColor(colorHeaderSub);
                    sc.setPadding(3);
                    subTablaParts.addCell(sc);
                }

                // Obtenemos la lista de participaciones directamente de la inscripción actual
                // NOTA: Si en tu Inscripcion.java la lista se llama diferente (ej: getParticipantesSeleccionados()), cambia este getter.
                    List<Participacion> listaParticipaciones = ins.getParticipaciones();

                if (listaParticipaciones != null && !listaParticipaciones.isEmpty()) {
                    for (Participacion p : listaParticipaciones) {
                        if (p == null) continue;

                        // Extraemos el participante asociado a la fila intermedia
                        Participante participante = p.getParticipante();
                        
                        String pNombre = (participante != null && participante.getNombre() != null) ? participante.getNombre() : "Sin nombre";
                        String pDni = (participante != null && participante.getDni() != null) ? participante.getDni() : "-";
                        String pFechaNac = "-";
                        if (participante != null && participante.getFechaNacimiento() != null) {
                            pFechaNac = String.valueOf(participante.getFechaNacimiento());
                        }

                        // SOLUCIÓN: El rol se extrae directamente del objeto Participacion (p), no de Participante
                        String pRol = p.getRol() != null ? String.valueOf(p.getRol()) : "Componente";

                        subTablaParts.addCell(new PdfPCell(new Paragraph(pNombre, fuenteMiniNormal)));
                        subTablaParts.addCell(new PdfPCell(new Paragraph(pDni, fuenteMiniNormal)));
                        subTablaParts.addCell(new PdfPCell(new Paragraph(pFechaNac, fuenteMiniNormal)));
                        subTablaParts.addCell(new PdfPCell(new Paragraph(pRol, fuenteMiniNormal)));
                    }
                } else {
                    PdfPCell vacia = new PdfPCell(new Paragraph("No constan participantes en esta inscripción.", fuenteMiniNormal));
                    vacia.setColspan(3);
                    vacia.setPadding(3);
                    subTablaParts.addCell(vacia);
                }

                celdaContenedorParticipantes.addElement(subTablaParts);
                tabla.addCell(celdaContenedorParticipantes);
            }

            documento.add(tabla);
            documento.close();
            return baos.toByteArray();
        
        } catch (Exception e) {
            System.err.println("Error al generar PDF General: " + e.getMessage());
            e.printStackTrace();
            return new byte[0];
        }
    }
    // ==========================================
    // PDF 2: COMPONENTES DE SELECCIONADOS (Multi-Página)
    // ==========================================
    public byte[] generarPdfParticipantesMultiples(List<Inscripcion> inscripcionesSeleccionadas) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(documento, baos);
            documento.open();

            // Definimos el formateador para la fecha de impresión
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            for (int i = 0; i < inscripcionesSeleccionadas.size(); i++) {
                Inscripcion ins = inscripcionesSeleccionadas.get(i);
                
                if (i > 0) {
                    documento.newPage(); // Salto de página para que cada ficha comience limpia
                }

                String nombreConcurso = (ins.getConcurso() != null && ins.getConcurso().getNombre() != null) ? ins.getConcurso().getNombre() : "CONCURSO";
                documento.add(crearEstructuraCabecera("FICHA DE INSCRIPCIÓN Y COMPONENTES", nombreConcurso));
                documento.add(new LineSeparator(2f, 100f, new Color(43, 138, 62), Element.ALIGN_CENTER, -2));
                documento.add(new Paragraph(" "));

                // === NUEVO: FECHA DE IMPRESIÓN EN CADA FICHA ===
                String fechaHoraActual = LocalDateTime.now().format(formatter);
                Paragraph pFecha = new Paragraph("Fecha y hora de impresión: " + fechaHoraActual, fuenteTextoNormal);
                pFecha.setAlignment(Element.ALIGN_RIGHT);
                documento.add(pFecha);
                documento.add(new Paragraph(" "));
                // ===============================================

                // Parseo e inserción de los metadatos de la agrupación actual
                String nombreAgrup = (ins.getAgrupacion() != null && ins.getAgrupacion().getNombre() != null) ? ins.getAgrupacion().getNombre() : "Sin nombre";
                String categoria = (ins.getAgrupacion() != null && ins.getAgrupacion().getCategoria() != null) ? String.valueOf(ins.getAgrupacion().getCategoria()) : "Sin categoría";
                String estado = (ins.getEstadoInscripcion() != null) ? String.valueOf(ins.getEstadoInscripcion()) : "PENDIENTE";

                Paragraph pDatos = new Paragraph();
                pDatos.setFont(fuenteTextoNormal);
                pDatos.add(new Chunk("Agrupación: ", fuenteTextoNegrita));
                pDatos.add(nombreAgrup + "\n");
                pDatos.add(new Chunk("Categoría: ", fuenteTextoNegrita));
                pDatos.add(categoria + "\n");
                pDatos.add(new Chunk("Estado del Registro: ", fuenteTextoNegrita));
                pDatos.add(estado + "\n");
                pDatos.setSpacingAfter(15);
                documento.add(pDatos);

                documento.add(new Paragraph("LISTA DE COMPONENTES", fuenteSeccion));
                documento.add(new Paragraph(" "));

                // Tabla estructurada para los participantes
                PdfPTable tablaComp = new PdfPTable(4);
                tablaComp.setWidthPercentage(100);
                tablaComp.setWidths(new float[]{40f, 18f, 17f, 25f});

                tablaComp.addCell(new PdfPCell(new Paragraph("Nombre Completo", fuenteTextoNegrita)));
                tablaComp.addCell(new PdfPCell(new Paragraph("DNI/NIE", fuenteTextoNegrita)));
                tablaComp.addCell(new PdfPCell(new Paragraph("F. Nacimiento", fuenteTextoNegrita)));
                tablaComp.addCell(new PdfPCell(new Paragraph("Rol / Instrumento", fuenteTextoNegrita)));

                if (ins.getParticipaciones() != null && !ins.getParticipaciones().isEmpty()) {
                    for (Participacion part : ins.getParticipaciones()) {
                        if (part == null) continue;
                        var p = part.getParticipante();
                        if (p != null) {
                            String nombreCompleto = p.getNombre() != null ? p.getNombre() : "Sin nombre";
                            String dni = p.getDni() != null ? p.getDni() : "--------";
                            String fNac = p.getFechaNacimiento() != null ? p.getFechaNacimiento().toString() : "-------";
                            String rol = part.getRol() != null ? String.valueOf(part.getRol()) : "COMPONENTE";

                            tablaComp.addCell(new PdfPCell(new Paragraph(nombreCompleto, fuenteFilaComponente)));
                            tablaComp.addCell(new PdfPCell(new Paragraph(dni, fuenteFilaComponente)));
                            tablaComp.addCell(new PdfPCell(new Paragraph(fNac, fuenteFilaComponente)));
                            tablaComp.addCell(new PdfPCell(new Paragraph(rol, fuenteFilaComponente)));
                        }
                    }
                } else {
                    PdfPCell vacia = new PdfPCell(new Paragraph("No constan participantes registrados en esta agrupación.", fuenteFilaComponente));
                    vacia.setColspan(4);
                    vacia.setPadding(5);
                    tablaComp.addCell(vacia);
                }
                documento.add(tablaComp);
            }

            documento.close();
            return baos.toByteArray();
        } catch (Exception e) {
            System.err.println("Error al generar PDF de Seleccionados: " + e.getMessage());
            return new byte[0];
        }
    }

    // ==========================================
    // MÉTODO AUXILIAR: CABECERA CORPORATIVA FHA
    // ==========================================
    private PdfPTable crearEstructuraCabecera(String tituloDocumento, String nombreConcurso) {
        PdfPTable tablaCabecera = new PdfPTable(2);
        tablaCabecera.setWidthPercentage(100);
        tablaCabecera.setWidths(new float[]{75f, 25f});
        
        PdfPCell celdaIzquierda = new PdfPCell();
        celdaIzquierda.setBorder(PdfPCell.NO_BORDER);
        celdaIzquierda.setVerticalAlignment(Element.ALIGN_BOTTOM);
        
        Paragraph pTitulo = new Paragraph(tituloDocumento, fuenteTitulo);
        pTitulo.setSpacingAfter(4);
        celdaIzquierda.addElement(pTitulo);
        
        Paragraph pSubtitulo = new Paragraph("ElPapelillo App • " + nombreConcurso.toUpperCase(), fuenteSubtitulo);
        celdaIzquierda.addElement(pSubtitulo);
        tablaCabecera.addCell(celdaIzquierda);
        
        PdfPCell celdaDerecha = new PdfPCell();
        celdaDerecha.setBorder(PdfPCell.NO_BORDER);
        celdaDerecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        try {
            Image logo = Image.getInstance("src/main/resources/static/images/Logo-ElPapelillo.png");
            logo.scaleToFit(65, 65);
            logo.setAlignment(Element.ALIGN_RIGHT);
            celdaDerecha.addElement(logo);
        } catch (Exception e) {
            // Se ignora si no encuentra la imagen física en el disco de desarrollo
        }
        
        tablaCabecera.addCell(celdaDerecha);
        return tablaCabecera;
    }

    public List<Inscripcion> obtenerInscripcionesPorConcurso(Long idConcurso) {
        return inscripcionRepository.findByConcursoIdManual(idConcurso);
    }

    public byte[] generarPdfSeleccionadosPorIds(List<Integer> idsInscripciones) {
    List<Inscripcion> inscripcionesSeleccionadas = new ArrayList<>();
    
    for (Integer id : idsInscripciones) {
        if (id == null) continue;
        
        // Usamos tu método del repositorio que devuelve List<Inscripcion> filtrado por Integer
        List<Inscripcion> resultado = inscripcionRepository.findByIdInscripcion(id);
        
        if (resultado != null && !resultado.isEmpty()) {
            inscripcionesSeleccionadas.add(resultado.get(0));
        }
    }
    
    // Llamamos a tu generador de PDF que ya procesa la lista de objetos Inscripcion
    return generarPdfParticipantesMultiples(inscripcionesSeleccionadas);
}
}


