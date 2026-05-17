import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms'; 

@Component({
  selector: 'detalle-agrupacion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pagina-agrupacion.html',
  styleUrls: ['./pagina-agrupacion.css']
})
export class DetalleAgrupacionComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  private cdRef = inject(ChangeDetectorRef);

  idInscripcion!: number;
  inscripcion: any = null; 
  participantes: any[] = []; 

  // Control visual del botón dinámico de la fianza (Tabla independiente 'fianza')
  fianzaSubida: boolean = false;

  // Lista de requerimientos comunes mapeados con tu base de datos (Aa, Bb, Cc, Dd)
  documentosRequeridos: any[] = [];

  // Paginación para las tablas/listas de la pantalla
  paginaActual: number = 1;
  elementosPorPagina: number = 5;

  ngOnInit(): void {
    this.idInscripcion = Number(this.route.snapshot.paramMap.get('id')) || 1;
    this.cargarDetalleInscripcion();
  }

  /**
   * Carga los datos de la inscripción y de forma anidada sus documentos desde la BD
   */
  cargarDetalleInscripcion(): void {
    // 1. Primera petición: Datos generales de la inscripción (Contiene relación FK con Fianza y Agrupación)
    this.http.get(`http://localhost:8080/api/inscripciones/${this.idInscripcion}`).subscribe({
      next: (data: any) => {
        this.inscripcion = data;
        this.participantes = data?.agrupacion?.participantes || [];

        // Evaluamos si ya tiene el objeto fianza en su tabla independiente
        this.comprobarEstadoFianza();

        // 2. Segunda petición anidada: Buscamos los documentos comunes reales de esta inscripción (Aa, Bb, Cc, Dd)
        this.http.get<any[]>(`http://localhost:8080/api/documentos/inscripcion/${this.idInscripcion}`).subscribe({
          next: (docs) => {
            if (docs && docs.length > 0) {
              this.documentosRequeridos = docs;
            } else {
              this.documentosRequeridos = [];
            }
            this.cdRef.detectChanges();
          },
          error: (errDocs) => {
            console.error('Error al obtener documentos reales:', errDocs);
            this.cdRef.detectChanges();
          }
        });

      },
      error: (errInsc) => {
        console.error('Error general al cargar la inscripción:', errInsc);
        this.cdRef.detectChanges();
      }
    }); 
  }

  /**
   * Comprueba si la fianza está registrada en su tabla independiente relacionada con la inscripción
   */
  comprobarEstadoFianza(): void {
    if (!this.inscripcion) {
      this.fianzaSubida = false;
      return;
    }
    
    const tieneObjetoFianza = this.inscripcion.fianza != null;
    const tieneRecibo = this.inscripcion.fianza?.rutaRecibo && this.inscripcion.fianza?.rutaRecibo.trim() !== '';
    const estaPagada = this.inscripcion.fianza?.pagada === true || this.inscripcion.fianza?.pagada === 1;

    if (tieneObjetoFianza && (tieneRecibo || estaPagada)) {
      this.fianzaSubida = true;
    } else {
      this.fianzaSubida = false;
    }
    console.log("¿La fianza está subida en su propia tabla?:", this.fianzaSubida);
  }

  /**
   * Captura el archivo PDF de la fianza elegido por el usuario y lo envía a la tabla 'fianza'
   */
  onFianzaSeleccionada(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);
    formData.append('idInscripcion', this.idInscripcion.toString());
    formData.append('usuarioId', '1'); // ID para el log de auditoría del backend

    // Envío por POST al endpoint encargado de registrar en la tabla independiente 'fianza'
    this.http.post('http://localhost:8080/api/fianzas/upload', formData).subscribe({
      next: (response: any) => {
        alert(`¡Éxito! El recibo de fianza "${file.name}" se ha guardado en su tabla correspondiente.`);
        this.cargarDetalleInscripcion(); // Recargamos para actualizar 'inscripcion.fianza' y ocultar el botón
      },
      error: (err) => {
        console.error('Error al subir la fianza:', err);
        alert('Hubo un error en el servidor al subir el archivo de fianza.');
      }
    });
  }

  /**
   * Abre el archivo en una pestaña nueva del navegador usando la URL del servidor
   */
  verDocumento(doc: any): void {
    if (!doc.urlArchivo) {
      alert('Este documento no tiene ningún archivo adjunto todavía.');
      return;
    }
    window.open(`http://localhost:8080/${doc.urlArchivo}`, '_blank');
  }

  /**
   * Cambia el estado de los documentos comunes (Aprobar/Rechazar en la tabla 'documento')
   */
  evaluarDocumento(doc: any, nuevoEstado: 'APROBADO' | 'PENDIENTE' | 'RECHAZADO'): void {
    if (!doc.comentarioRevision) {
      doc.comentarioRevision = ''; 
    }
    doc.estado = nuevoEstado;
    
    this.http.put(`http://localhost:8080/api/documentos/${doc.idDocumento}`, {
      idDocumento: doc.idDocumento,
      comentarioRevision: doc.comentarioRevision,
      nombre: doc.nombre,
      tipo: doc.tipo,
      urlArchivo: doc.urlArchivo,
      estado: doc.estado
    }).subscribe({
      next: () => {
        alert(`Documento "${doc.nombre}" evaluado como ${nuevoEstado} con éxito.`);
        this.cargarDetalleInscripcion(); // Refresca los estados en la pantalla
      },
      error: (err) => console.error('Error al evaluar el documento:', err)
    });
  }

  /**
   * Cambia el estado global de la inscripción completa (Aprobar/Rechazar inscripción)
   */
  actualizarEstadoInscripcion(nuevoEstado: 'APROBADO' | 'RECHAZADO'): void {
    if (!this.idInscripcion) return;
    
    this.http.put(`http://localhost:8080/api/inscripciones/${this.idInscripcion}/estado`, { estado: nuevoEstado }).subscribe({
      next: () => {
        alert(`Estado de la inscripción cambiado globalmente a ${nuevoEstado}.`);
        this.cargarDetalleInscripcion();
      },
      error: (err) => console.error('Error al actualizar el estado global:', err)
    });
  }

  // ==========================================
  // MÉTODOS DE PAGINACIÓN 
  // ==========================================
  
  /**
   * Paginador 1: Corta los PARTICIPANTES (los músicos de la tabla inferior) de 5 en 5
   */
  get participantesPaginados(): any[] {
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina;
    const fin = inicio + this.elementosPorPagina;
    return this.participantes.slice(inicio, fin);
  }

  /**
   * Paginador 2: Corta los DOCUMENTOS COMUNES de 5 en 5 (Evita el error del HTML si usas documentosPaginados)
   */
  get documentosPaginados(): any[] {
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina;
    const fin = inicio + this.elementosPorPagina;
    return this.documentosRequeridos.slice(inicio, fin);
  }

  get totalPaginas(): number {
    // Tomamos como base la lista de participantes para el cálculo de botones siguientes/anteriores
    return Math.ceil(this.participantes.length / this.elementosPorPagina);
  }

  paginaSiguiente(): void { 
    if (this.paginaActual < this.totalPaginas) this.paginaActual++; 
  }
  
  paginaAnterior(): void { 
    if (this.paginaActual > 1) this.paginaActual--; 
  }
  
  obtenerIndiceInicial(): number { 
    return (this.paginaActual - 1) * this.elementosPorPagina; 
  }
  
  obtenerIndiceFinal(): number {
    const resultante = this.paginaActual * this.elementosPorPagina;
    return resultante > this.participantes.length ? this.participantes.length : resultante;
  }

  descargarPDF(): void { 
    alert('Descargando el listado oficial de componentes...'); 
  }
  
  volver(): void { 
    this.router.navigate(['/panel-control-administrador']); 
  }
}