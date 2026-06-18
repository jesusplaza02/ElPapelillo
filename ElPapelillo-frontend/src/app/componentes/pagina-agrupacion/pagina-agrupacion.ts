import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms'; 
import { Location } from '@angular/common';

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
  private location = inject(Location);

  idInscripcion!: number;
  inscripcion: any = null; 
  participantes: any[] = []; 

  fianzaSubida: boolean = false;
  mostrandoFormularioFianza: boolean = false;
  esHistorico: boolean = false;

  datosFianza = {
    importe: 300, 
    fechaPago: new Date().toISOString().substring(0, 16) 
  };
  archivoFianzaSeleccionado: File | null = null;

  documentosRequeridos: any[] = [];

  paginaActual: number = 1;
  paginaActualDocs: number = 1;
  elementosPorPagina: number = 5;

  mostrarModalExito: boolean = false;
  mensajeModalExito: string = '';
  
  mostrarModalError: boolean = false;
  tituloModalError: string = '';
  contenidoModalError: string = '';

  mostrarModalConfirmarBorrado: boolean = false;

  ngOnInit(): void {
    this.idInscripcion = Number(this.route.snapshot.paramMap.get('id')) || 1;
    this.cargarDetalleInscripcion();
  }

  cargarDetalleInscripcion(): void {
    const adminId = localStorage.getItem('idUsuario') || localStorage.getItem('idAdministrador') || localStorage.getItem('id') || '1';

    this.http.get(`http://localhost:8080/api/inscripciones/${this.idInscripcion}?idUsuarioActual=${adminId}`).subscribe({
      next: (data: any) => {
        this.inscripcion = data;
        this.participantes = data?.agrupacion?.participantes || [];
        
        const estadoEnum = data?.concurso?.estadoConcurso; 

        if (estadoEnum && String(estadoEnum).trim().toUpperCase() === 'HISTORICO') {
          this.esHistorico = true;
        } else {
          this.esHistorico = false;
        }

        this.comprobarEstadoFianza();
        this.cdRef.detectChanges(); 

        this.http.get<any[]>(`http://localhost:8080/api/documentos/inscripcion/${this.idInscripcion}?idUsuarioActual=${adminId}`).subscribe({
          next: (docs) => {
            this.documentosRequeridos = docs || [];
            this.paginaActualDocs = 1;
            this.cdRef.detectChanges();
          },
          error: (errDocs: HttpErrorResponse) => {
            console.error(errDocs);
            this.cdRef.detectChanges();
          }
        });
      },
      error: (errInsc: HttpErrorResponse) => {
        console.error(errInsc);
        if (errInsc.status === 403) {
          this.tituloModalError = 'Acceso Restringido';
          this.contenidoModalError = 'Acceso denegado: Esta agrupación pertenece a una organización o concurso que no gestionas.';
          this.mostrarModalError = true;

          setTimeout(() => {
            this.router.navigate(['/panel-control-administrador']); 
          }, 3000);
        }
        this.cdRef.detectChanges();
      }
    }); 
  }

  comprobarEstadoFianza(): void {
    if (!this.inscripcion) {
      this.fianzaSubida = false;
      return;
    }
    
    const fianza = this.inscripcion.fianza;
    const idFianzaPrimitivo = this.inscripcion.id_fianza || this.inscripcion.idFianza;
    const ruta = fianza?.ruta_recibo || fianza?.rutaRecibo;

    if ((fianza && ruta && ruta.trim() !== '') || (idFianzaPrimitivo && idFianzaPrimitivo !== null)) {
      this.fianzaSubida = true;
    } else {
      this.fianzaSubida = false;
    }
  }

  private registrarAuditoria(accion: string, descripcion: string): void {
    const adminIdGuardado = localStorage.getItem('idUsuario') || 
                            localStorage.getItem('idAdministrador') || 
                            localStorage.getItem('id');
    
    const administradorId = adminIdGuardado ? Number(adminIdGuardado) : 1;

    const payloadAuditoria = {
      administradorId: administradorId, 
      accion: accion,                    
      descripcion: descripcion            
    };

    this.http.post('http://localhost:8080/api/auditoria', payloadAuditoria).subscribe({
      next: () => {},
      error: (err) => console.error(err)
    });
  }

  abrirFormularioFianza(): void {
    if (this.esHistorico) return; 
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
        this.tituloModalError = 'Formato de Fichero Incorrecto';
        this.contenidoModalError = 'El resguardo contable adjunto debe ser de forma obligatoria un documento en formato PDF.';
        this.mostrarModalError = true;
        event.target.value = ''; 
        return;
      }
      this.archivoFianzaSeleccionado = file;
    }
  }

  guardarFianzaCompleta(): void {
    if (this.esHistorico) return; 

    if (!this.archivoFianzaSeleccionado) {
      this.tituloModalError = 'Falta el Documento';
      this.contenidoModalError = 'Debes adjuntar el archivo digital en PDF correspondiente al resguardo del banco.';
      this.mostrarModalError = true;
      return;
    }

    if (!this.datosFianza.importe || this.datosFianza.importe <= 0) {
      this.tituloModalError = 'Importe no Válido';
      this.contenidoModalError = 'Por favor, introduce una cifra económica superior a cero euros para proceder al asiento.';
      this.mostrarModalError = true;
      return;
    }

    if (!this.datosFianza.fechaPago || this.datosFianza.fechaPago.trim() === '') {
      this.tituloModalError = 'Fecha Obligatoria';
      this.contenidoModalError = 'Es indispensable definir el momento exacto en que el representante efectuó el ingreso.';
      this.mostrarModalError = true;
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
          this.mostrandoFormularioFianza = false;
          this.archivoFianzaSeleccionado = null;

          const nombreAgrup = this.inscripcion?.agrupacion?.nombre || 'Agrupación';
          this.registrarAuditoria(
            'SUBIDA_FIANZA', 
            `Se ha registrado la fianza de ${this.datosFianza.importe}€ para la agrupación: ${nombreAgrup}.`
          );

          this.mensajeModalExito = 'Los datos contables de la fianza y su correspondiente extracto digital han quedado vinculados correctamente en el expediente.';
          this.mostrarModalExito = true;
          this.cargarDetalleInscripcion(); 
        },
        error: (err: HttpErrorResponse) => {
          this.tituloModalError = 'Error de Servidor';
          this.contenidoModalError = 'No se ha podido almacenar la fianza. Verifica la integridad técnica del backend.';
          this.mostrarModalError = true;
        }
      });
  }

  solicitarConfirmacionBorradoFianza(): void {
    if (this.esHistorico) return;
    this.mostrarModalConfirmarBorrado = true;
  }

  ejecutarEliminarFianza(): void {
    if (this.esHistorico) return;
    this.mostrarModalConfirmarBorrado = false;

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

        this.mensajeModalExito = 'El documento de fianza y su histórico económico se han borrado de forma definitiva.';
        this.mostrarModalExito = true;
        this.cargarDetalleInscripcion(); 
      },
      error: (err: HttpErrorResponse) => {
        this.tituloModalError = 'Denegación de Borrado';
        this.contenidoModalError = 'El servidor no ha respondido adecuadamente a la petición de borrado.';
        this.mostrarModalError = true;
      }
    });
  }

  evaluarDocumento(doc: any, nuevoEstado: 'APROBADO' | 'PENDIENTE' | 'RECHAZADO'): void {
    if (this.esHistorico) return;

    if (!doc.comentarioRevision) doc.comentarioRevision = ''; 
    doc.estado = nuevoEstado;
    
    const documentoPayload = {
      idDocumento: doc.idDocumento,
      nombre: doc.nombre,
      tipo: doc.tipo,
      urlArchivo: doc.urlArchivo,
      estado: doc.estado,
      comentarioRevision: doc.comentarioRevision,
      inscripcion: { idInscripcion: this.idInscripcion }
    };

    this.http.put(`http://localhost:8080/api/documentos/${doc.idDocumento}`, documentoPayload).subscribe({
      next: (response: any) => {
        const nombreAgrup = this.inscripcion?.agrupacion?.nombre || 'Agrupación';
        let descripcionAuditoria = `Se ha ${nuevoEstado.toLowerCase()} el documento "${doc.nombre}" de la agrupación: ${nombreAgrup}.`;
        
        if (nuevoEstado === 'RECHAZADO' && doc.comentarioRevision.trim() !== '') {
          descripcionAuditoria += ` Motivo del rechazo: "${doc.comentarioRevision}"`;
        }

        this.registrarAuditoria(`REVISIÓN_DOC_${nuevoEstado}`, descripcionAuditoria);
        
        this.mensajeModalExito = `El documento "${doc.nombre}" ha sido actualizado a [${nuevoEstado}] correctamente.`;
        this.mostrarModalExito = true;
        this.cargarDetalleInscripcion(); 
      },
      error: (err: HttpErrorResponse) => {
        this.tituloModalError = 'Fallo de Persistencia';
        this.contenidoModalError = 'No se ha podido procesar el cambio de estado de la documentación.';
        this.mostrarModalError = true;
      }
    });
  }

  actualizarEstadoInscripcion(nuevoEstado: 'APROBADO' | 'RECHAZADO'): void {
    if (this.esHistorico || !this.idInscripcion) return;
    
    this.http.put(`http://localhost:8080/api/inscripciones/${this.idInscripcion}/estado`, { estado: nuevoEstado }).subscribe({
      next: () => {
        const nombreAgrup = this.inscripcion?.agrupacion?.nombre || 'Agrupación';
        this.registrarAuditoria(
          `INSCRIPCION_${nuevoEstado}`, 
          `Se ha determinado el estado global de la agrupación "${nombreAgrup}" como ${nuevoEstado}.`
        );
        this.mensajeModalExito = `La resolución global de esta solicitud de inscripción ha quedado guardada como: ${nuevoEstado}.`;
        this.mostrarModalExito = true;
        this.cargarDetalleInscripcion();
      },
      error: (err: HttpErrorResponse) => {
        this.tituloModalError = 'Error de Transición';
        this.contenidoModalError = 'Hubo una anomalía interna al intentar cambiar el estado global de la inscripción.';
        this.mostrarModalError = true;
      }
    });
  }

  get participantesPaginados(): any[] {
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina;
    return this.participantes.slice(inicio, inicio + this.elementosPorPagina);
  }

  get documentosPaginados(): any[] {
    const inicio = (this.paginaActualDocs - 1) * this.elementosPorPagina;
    return this.documentosRequeridos.slice(inicio, inicio + this.elementosPorPagina);
  }

  get totalPaginas(): number {
    return Math.ceil(this.documentosRequeridos.length / this.elementosPorPagina) || 1;
  }

  paginaSiguiente(): void { 
    if (this.paginaActualDocs < this.totalPaginas) {
      this.paginaActualDocs++; 
      this.cdRef.detectChanges();
    }
  }
  
  paginaAnterior(): void { 
    if (this.paginaActualDocs > 1) {
      this.paginaActualDocs--; 
      this.cdRef.detectChanges();
    }
  }
  
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
        
        document.body.removeChild(enlaceFantasma);
        window.URL.revokeObjectURL(urlLocal);

        this.registrarAuditoria(
          'DESCARGA_PDF_COMPONENTES', 
          `El administrador ha descargado el PDF oficial de componentes de la agrupación: ${this.inscripcion?.agrupacion?.nombre}.`
        );
      },
      error: (err: HttpErrorResponse) => {
        this.tituloModalError = 'Error en Generación de PDF';
        this.contenidoModalError = 'Los servicios de JasperReports no han podido compilar el listado oficial de componentes.';
        this.mostrarModalError = true;
      }
    });
  }
  
  volver(): void {
    this.location.back();
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
      error: (err) => {
        this.tituloModalError = 'Fichero No Encontrado';
        this.contenidoModalError = 'El fichero solicitado no se localiza en el volumen físico del backend.';
        this.mostrarModalError = true;
      }
    });
  }
}