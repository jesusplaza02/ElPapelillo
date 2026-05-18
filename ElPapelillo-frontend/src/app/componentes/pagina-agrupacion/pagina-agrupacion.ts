import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
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

  // Control visual del bloque de fianza
  fianzaSubida: boolean = false;
  mostrandoFormularioFianza: boolean = false;

  // Variables para enlazar los inputs del formulario en el HTML
  datosFianza = {
    importe: 300, // Valor inicial por defecto
    fechaPago: new Date().toISOString().substring(0, 16) // Fecha actual local
  };
  archivoFianzaSeleccionado: File | null = null;

  // Lista de requerimientos comunes mapeados (Aa, Bb, Cc, Dd)
  documentosRequeridos: any[] = [];

  // Paginación para las tablas/listas
  paginaActual: number = 1;
  elementosPorPagina: number = 5;

  ngOnInit(): void {
    this.idInscripcion = Number(this.route.snapshot.paramMap.get('id')) || 1;
    this.cargarDetalleInscripcion();
  }

  /**
   * Carga los datos de la inscripción y sus documentos desde la BD
   */
  cargarDetalleInscripcion(): void {
    this.http.get(`http://localhost:8080/api/inscripciones/${this.idInscripcion}`).subscribe({
      next: (data: any) => {
        this.inscripcion = data;
        this.participantes = data?.agrupacion?.participantes || [];

        // Evaluamos el estado de la fianza con seguridad
        this.comprobarEstadoFianza();

        // Buscamos los documentos comunes reales de esta inscripción
        this.http.get<any[]>(`http://localhost:8080/api/documentos/inscripcion/${this.idInscripcion}`).subscribe({
          next: (docs) => {
            this.documentosRequeridos = docs || [];
            this.cdRef.detectChanges();
          },
          error: (errDocs: HttpErrorResponse) => {
            console.error('Error al obtener documentos reales:', errDocs);
            this.cdRef.detectChanges();
          }
        });
      },
      error: (errInsc: HttpErrorResponse) => {
        console.error('Error general al cargar la inscripción:', errInsc);
        this.cdRef.detectChanges();
      }
    }); 
  }

  /**
   * Comprueba de forma segura si la fianza existe controlando los valores nulos/undefined
   */
  comprobarEstadoFianza(): void {
    if (!this.inscripcion) {
      this.fianzaSubida = false;
      return;
    }
    
    // Si la relación fianza no viene cargada, comprobamos también el campo primitivo id_fianza de la tabla
    const fianza = this.inscripcion.fianza;
    const idFianzaPrimitivo = this.inscripcion.id_fianza || this.inscripcion.idFianza;
    const ruta = fianza?.ruta_recibo || fianza?.rutaRecibo;

    if ((fianza && ruta && ruta.trim() !== '') || (idFianzaPrimitivo && idFianzaPrimitivo !== null)) {
      this.fianzaSubida = true;
    } else {
      this.fianzaSubida = false;
    }
    
    console.log("¿Estado visual de la fianza activo?:", this.fianzaSubida);
    console.log("Datos de la fianza detectados en el JSON:", fianza);
  }

  // ==========================================
  // GESTIÓN DE AUDITORÍAS (TABLA logauditoria)
  // ==========================================
  
  /**
   * Registra una entrada en la tabla 'logauditoria' respetando la relación @ManyToOne de Java
   */
  /**
   * Registra una entrada en la tabla 'logauditoria' obteniendo el ID
   * del administrador activo desde el localStorage.
   */
  private registrarAuditoria(accion: string, descripcion: string): void {
    // Intentamos recuperar el ID del administrador logueado desde el localStorage
    // REVISA AQUÍ: Cambia el string si en tu login lo guardaste con otra clave (ej: 'id', 'idUsuario', etc.)
    const adminIdGuardado = localStorage.getItem('idUsuario') || 
                            localStorage.getItem('idAdministrador') || 
                            localStorage.getItem('id');
    
    // Si por algún motivo no hay nadie logueado (sesión expirada), ponemos el ID 1 por seguridad
    const administradorId = adminIdGuardado ? Number(adminIdGuardado) : 1;

    // Estructuramos un payload plano y seguro para el Map de Java
    const payloadAuditoria = {
      administradorId: administradorId, 
      accion: accion,                     
      descripcion: descripcion            
    };

    // Envío al endpoint en singular
    this.http.post('http://localhost:8080/api/auditoria', payloadAuditoria).subscribe({
      next: () => console.log(`[Auditoría] Registro guardado con éxito para el Admin ID (${administradorId}): ${accion}`),
      error: (err) => console.error('[Auditoría] Error al insertar en logauditoria:', err)
    });
  }
  // ==========================================
  // FORMULARIO Y LÓGICA DE FIANZA
  // ==========================================

  abrirFormularioFianza(): void {
    this.mostrandoFormularioFianza = true;
  }

  cancelarFianza(): void {
    this.mostrandoFormularioFianza = false;
    this.archivoFianzaSeleccionado = null;
  }

  prepararArchivoFianza(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.type !== 'application/pdf') {
        alert('Por favor, selecciona un documento PDF válido.');
        event.target.value = ''; 
        return;
      }
      this.archivoFianzaSeleccionado = file;
    }
  }

  guardarFianzaCompleta(): void {
    if (!this.archivoFianzaSeleccionado) {
      alert('Error: Debes adjuntar obligatoriamente el archivo PDF del resguardo.');
      return;
    }

    if (!this.datosFianza.importe || this.datosFianza.importe <= 0) {
      alert('Error: Debes introducir un importe válido para la fianza.');
      return;
    }

    if (!this.datosFianza.fechaPago || this.datosFianza.fechaPago.trim() === '') {
      alert('Error: Debes introducir la fecha y hora en la que se realizó el pago.');
      return;
    }

    const formData = new FormData();
    formData.append('file', this.archivoFianzaSeleccionado);
    formData.append('importe', this.datosFianza.importe.toString());
    
    const fechaFormateada = new Date(this.datosFianza.fechaPago).toISOString().split('.')[0]; 
    formData.append('fechaPago', fechaFormateada);

    this.http.post(`http://localhost:8080/api/fianzas/upload/${this.idInscripcion}`, formData)
      .subscribe({
        next: (res: any) => {
          alert('¡Fianza y datos contables registrados con éxito!');
          this.mostrandoFormularioFianza = false;
          this.archivoFianzaSeleccionado = null;

          const nombreAgrup = this.inscripcion?.agrupacion?.nombre || 'Agrupación';
          this.registrarAuditoria(
            'SUBIDA_FIANZA', 
            `Se ha registrado la fianza de ${this.datosFianza.importe}€ para la agrupación: ${nombreAgrup}.`
          );

          this.cargarDetalleInscripcion(); 
        },
        error: (err: HttpErrorResponse) => {
          console.error('Error al guardar fianza completa:', err);
          alert('Error del servidor al registrar la fianza.');
        }
      });
  }

  eliminarFianza(): void {
    if (!this.idInscripcion) return;

    const confirmar = confirm('¿Estás seguro de que deseas eliminar el documento de fianza de esta inscripción?');
    if (!confirmar) return;

    this.http.delete(`http://localhost:8080/api/fianzas/inscripcion/${this.idInscripcion}`).subscribe({
      next: (res: any) => {
        if (this.inscripcion) {
          this.inscripcion.id_fianza = null;
          this.inscripcion.fianza = null;
        }
        this.fianzaSubida = false; 
        
        const nombreAgrup = this.inscripcion?.agrupacion?.nombre || 'Agrupación';
        this.registrarAuditoria(
          'BORRADO_FIANZA', 
          `Se ha eliminado permanentemente el registro de fianza asociado a la agrupación: ${nombreAgrup}.`
        );

        alert('Fianza eliminada con éxito de la base de datos.');
        this.cargarDetalleInscripcion(); 
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error al intentar eliminar la fianza del servidor:', err);
        alert('No se pudo eliminar la fianza.');
      }
    });
  }

  // ==========================================
  // EVALUACIÓN DE DOCUMENTOS E INSCRIPCIÓN
  // ==========================================

  evaluarDocumento(doc: any, nuevoEstado: 'APROBADO' | 'PENDIENTE' | 'RECHAZADO'): void {
    if (!doc.comentarioRevision) doc.comentarioRevision = ''; 
    doc.estado = nuevoEstado;
    
    const documentoDvd = {
      idDocumento: doc.idDocumento,
      nombre: doc.nombre,
      tipo: doc.tipo,
      urlArchivo: doc.urlArchivo,
      estado: doc.estado,
      comentarioRevision: doc.comentarioRevision,
      inscripcion: { idInscripcion: this.idInscripcion }
    };

    this.http.put(`http://localhost:8080/api/documentos/${doc.idDocumento}`, documentoDvd).subscribe({
      next: (response: any) => {
        const nombreAgrup = this.inscripcion?.agrupacion?.nombre || 'Agrupación';
        let descripcionAuditoria = `Se ha ${nuevoEstado.toLowerCase()} el documento "${doc.nombre}" de la agrupación: ${nombreAgrup}.`;
        
        if (nuevoEstado === 'RECHAZADO' && doc.comentarioRevision.trim() !== '') {
          descripcionAuditoria += ` Motivo del rechazo: "${doc.comentarioRevision}"`;
        }

        this.registrarAuditoria(`REVISIÓN_DOC_${nuevoEstado}`, descripcionAuditoria);
        alert(`¡Estado guardado correctamente! El documento "${doc.nombre}" se ha actualizado a ${nuevoEstado}.`);
        this.cargarDetalleInscripcion(); 
      },
      error: (err: HttpErrorResponse) => alert('Error del servidor al guardar.')
    });
  }

  actualizarEstadoInscripcion(nuevoEstado: 'APROBADO' | 'RECHAZADO'): void {
    if (!this.idInscripcion) return;
    
    this.http.put(`http://localhost:8080/api/inscripciones/${this.idInscripcion}/estado`, { estado: nuevoEstado }).subscribe({
      next: () => {
        const nombreAgrup = this.inscripcion?.agrupacion?.nombre || 'Agrupación';
        this.registrarAuditoria(
          `INSCRIPCION_${nuevoEstado}`, 
          `Se ha determinado el estado global de la agrupación "${nombreAgrup}" como ${nuevoEstado}.`
        );
        alert(`Estado de la inscripción cambiado globalmente a ${nuevoEstado}.`);
        this.cargarDetalleInscripcion();
      },
      error: (err: HttpErrorResponse) => console.error('Error al actualizar el estado global:', err)
    });
  }

  // ==========================================
  // MÉTODOS DE PAGINACIÓN Y NAVEGACIÓN
  // ==========================================
  
  get participantesPaginados(): any[] {
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina;
    return this.participantes.slice(inicio, inicio + this.elementosPorPagina);
  }

  get documentosPaginados(): any[] {
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina;
    return this.documentosRequeridos.slice(inicio, inicio + this.elementosPorPagina);
  }

  get totalPaginas(): number {
    return Math.ceil(this.documentosRequeridos.length / this.elementosPorPagina) || 1;
  }

  paginaSiguiente(): void { 
    if (this.paginaActual < this.totalPaginas) this.paginaActual++; 
  }
  
  paginaAnterior(): void { 
    if (this.paginaActual > 1) this.paginaActual--; 
  }
  
  /**
   * CORREGIDO: Declaramos el método que faltaba y que reclamaba tu plantilla HTML
   */
  descargarPDF(): void { 
    if (!this.idInscripcion) return;

    const nombreAgrup = this.inscripcion?.agrupacion?.nombre?.replace(/ /g, '_') || 'Agrupacion';
    const url = `http://localhost:8080/api/inscripciones/${this.idInscripcion}/exportar-pdf`;

    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: (blob: Blob) => {
        const urlLocal = window.URL.createObjectURL(blob);
        const enlaceFantasma = document.createElement('a');
        enlaceFantasma.href = urlLocal;
        enlaceFantasma.download = `Listado_${nombreAgrup}.pdf`;
        document.body.appendChild(enlaceFantasma);
        enlaceFantasma.click();
        
        // Limpieza del DOM
        document.body.removeChild(enlaceFantasma);
        window.URL.revokeObjectURL(urlLocal);

        // Registramos la acción en la auditoría
        this.registrarAuditoria(
          'DESCARGA_PDF_COMPONENTES', 
          `El administrador ha descargado el PDF oficial de componentes de la agrupación: ${this.inscripcion?.agrupacion?.nombre}.`
        );
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error al descargar el PDF desde el servidor:', err);
        alert('No se pudo generar o descargar el archivo PDF en este momento.');
      }
    });
  }
  
  volver(): void { 
    this.router.navigate(['/panel-control-administrador']); 
  }

  descargarArchivo(rutaArchivo: string, nombreDescarga: string): void {
    if (!rutaArchivo) return;
    const urlCompleta = `http://localhost:8080/${rutaArchivo}`;

    this.http.get(urlCompleta, { responseType: 'blob' }).subscribe({
      next: (blob: Blob) => {
        const urlLocal = window.URL.createObjectURL(blob);
        const enlaceFantasma = document.createElement('a');
        enlaceFantasma.href = urlLocal;
        enlaceFantasma.download = nombreDescarga;
        enlaceFantasma.click();
        window.URL.revokeObjectURL(urlLocal);
      },
      error: (err) => console.error('Error al descargar el archivo:', err)
    });
  }
}