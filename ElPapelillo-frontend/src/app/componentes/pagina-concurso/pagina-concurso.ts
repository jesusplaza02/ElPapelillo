import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router} from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-detalle-concurso',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pagina-concurso.html',
  styleUrls: ['./pagina-concurso.css']
})
export class DetalleConcursoComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  private cdRef = inject(ChangeDetectorRef);

  idConcurso!: number;
  
  concurso: any = {
    nombre: 'Cargando Concurso...',
    fechaInicioInscripcion: '',
    fechaFinInscripcion: '',
    fechaInicio: '',
    fechaFin: '',
    estadoConcurso: 'ACTIVO' 
  };

  inscripciones: any[] = [];
  inscripcionesFiltradas: any[] = [];
  
  filtroTexto: string = '';
  filtroCategoria: string = '';
  filtroEstado: string = '';
  filtroFianza: string = '';

  esHistorico: boolean = false;

  stats = {
    totalGrupos: 0,
    pendientesValidar: 0,
    fianzasPendientes: 0
  };

  // GESTIÓN DE CIRCULARES INFORMATIVAS
  mostrarModalCircular: boolean = false;
  circularAsunto: string = '';
  circularCuerpo: string = '';
  archivosCircularSeleccionados: File[] = []; 
  enviandoCircular: boolean = false;          
  errorCircular: string | null = null;        

  // MODALES INTERNOS GENERALES
  mostrarModalExitoGlobal: boolean = false;
  tituloModalExitoGlobal: string = '';
  contenidoModalExitoGlobal: string = '';

  mostrarModalErrorGlobal: boolean = false;
  tituloModalErrorGlobal: string = '';
  contenidoModalErrorGlobal: string = '';

  ngOnInit(): void {
  this.idConcurso = Number(this.route.snapshot.paramMap.get('id'));
  
  this.cargarDatosConcurso();
}

  lanzarModalInformativo(titulo: string, contenido: string, tipo: 'success' | 'error'): void {
    if (tipo === 'success') {
      this.tituloModalExitoGlobal = titulo;
      this.contenidoModalExitoGlobal = contenido;
      this.mostrarModalExitoGlobal = true;
    } else {
      this.tituloModalErrorGlobal = titulo;
      this.contenidoModalErrorGlobal = contenido;
      this.mostrarModalErrorGlobal = true;
    }
    this.cdRef.detectChanges();
  }

  cargarDatosConcurso(): void {
    const idUsuarioLogueado = localStorage.getItem('idUsuario') || '0';
    this.http.get(`http://localhost:8080/api/concursos/${this.idConcurso}?idUsuarioActual=${idUsuarioLogueado}`).subscribe({
      next: (data: any) => {
        if (data) {
          const miRol = localStorage.getItem('rol')?.toUpperCase();
          const miIdOrg = localStorage.getItem('id_organizacion') ;
          const idOrgConcurso = data.id_organizacion;

          if (miRol !== 'SYSADMIN' && String(idOrgConcurso) !== String(miIdOrg)) {
            this.lanzarModalInformativo('Acceso Denegado', 'No tienes permisos para acceder a este concurso.', 'error');
            this.router.navigate(['/panel-control-administrador']);
            return;
          }

          this.concurso = data;
          const estadoEnum = data?.estadoConcurso;
          this.esHistorico = estadoEnum && String(estadoEnum).trim().toUpperCase() === 'HISTORICO';
          
          this.cdRef.detectChanges();
          this.cargarInscripciones(); 
        } else {
          this.router.navigate(['/panel-control-administrador']);
        }
      },
      error: (err) => {
        console.error('Error IDOR o Concurso ajeno:', err);
 
        this.concurso = null;
        this.cdRef.detectChanges();
        
        this.lanzarModalInformativo(
          'Acceso Restringido', 
          'No tienes autorización para visualizar este concurso. Redirigiendo...', 
          'error'
        );

        setTimeout(() => {
          this.mostrarModalErrorGlobal = false;
          this.router.navigate(['/panel-control-administrador']);
          this.cdRef.detectChanges();
        }, 3000);
      }
    });
  }

  conmutarEstadoConcurso(): void {
    if (!this.concurso) return;

    const nuevoEstado = this.esHistorico ? 'ACTIVO' : 'HISTORICO';
    
    const concursoModificado = {
      ...this.concurso,
      estadoConcurso: nuevoEstado
    };

    const url = `http://localhost:8080/api/concursos/${this.idConcurso}`;

    this.http.put(url, concursoModificado).subscribe({
      next: (concursoActualizado: any) => {
        this.concurso = concursoActualizado || concursoModificado;
        
        this.esHistorico = String(this.concurso.estadoConcurso).trim().toUpperCase() === 'HISTORICO';
        this.cdRef.detectChanges();

        this.registrarAuditoria(
          'CAMBIO_ESTADO_CONCURSO',
          `El administrador ha cambiado el estado del concurso "${this.concurso?.nombre}" a ${nuevoEstado}.`
        );

        this.lanzarModalInformativo(
          'Estado Actualizado', 
          `El concurso ahora se encuentra en estado: ${nuevoEstado}.`, 
          'success'
        );
      },
      error: (err) => {
        console.error('Error al conmutar el estado del concurso:', err);
        this.lanzarModalInformativo(
          'Error de Operación', 
          'No se pudo actualizar el estado utilizando el mapeo general.', 
          'error'
        );
      }
    });
  }
  cargarInscripciones(): void {
    this.http.get<any[]>(`http://localhost:8080/api/inscripciones/concurso/${this.idConcurso}`).subscribe({
      next: (data) => {
        this.inscripciones = data && data.length > 0 ? data : [];
        this.finalizarCarga();
      },
      error: (err) => {
        console.error('El backend ha dado un error para este concurso:', err);
        this.inscripciones = []; 
        this.finalizarCarga();
      }
    });
  }

  finalizarCarga(): void {
    this.inscripcionesFiltradas = [...this.inscripciones];
    this.calcularEstadisticas();
    this.cdRef.detectChanges();
  }

  calcularEstadisticas(): void {
    this.stats.totalGrupos = this.inscripciones.length;
    
    this.stats.pendientesValidar = this.inscripciones.filter(ins => 
      ins?.estadoInscripcion === 'PENDIENTE' || ins?.estadoInscripcion === 'Pendiente'
    ).length;
    
    this.stats.fianzasPendientes = this.inscripciones.filter(ins => 
      !ins?.fianza || ins?.fianza === 'PENDIENTE' || ins?.fianza === 'Pendiente'
    ).length;
  }

  filtrarAgrupaciones(): void {
    this.inscripcionesFiltradas = this.inscripciones.filter(ins => {
      const cumpleTexto = !this.filtroTexto.trim() || 
        ins.agrupacion?.nombre?.toLowerCase().includes(this.filtroTexto.toLowerCase()) ||
        ins.agrupacion?.representante?.nombre?.toLowerCase().includes(this.filtroTexto.toLowerCase());

      const cumpleCategoria = !this.filtroCategoria || 
        ins.agrupacion?.categoria?.toUpperCase() === this.filtroCategoria.toUpperCase();

      const cumpleEstado = !this.filtroEstado || 
        ins.estadoInscripcion?.toUpperCase() === this.filtroEstado.toUpperCase();

      let cumpleFianza = true;
      const tieneFianzaPagada = ins.fianza !== null && ins.fianza !== undefined && (ins.fianza.idFianza ?? ins.fianza.id) !== null;

      if (this.filtroFianza === 'PAGADA') {
        cumpleFianza = tieneFianzaPagada;
      } else if (this.filtroFianza === 'PENDIENTE') {
        cumpleFianza = !tieneFianzaPagada;
      }

      return cumpleTexto && cumpleCategoria && cumpleEstado && cumpleFianza;
    });
    this.cdRef.detectChanges();
  }

  irADetalleAgrupacion(idInscripcion: number): void {
    if (idInscripcion) {
      this.router.navigate(['/detalle-agrupacion', idInscripcion]);
    }
  }

  tieneSeleccionados(): boolean {
    if (!this.inscripcionesFiltradas) return false;
    return this.inscripcionesFiltradas.some((ins: any) => ins.seleccionado === true);
  }

  seleccionarTodos(event: any): void {
    if (!this.inscripcionesFiltradas) return;
    const checked = event.target.checked;
    this.inscripcionesFiltradas.forEach((ins: any) => ins.seleccionado = checked);
  }

  generarPDF(): void {
    if (!this.concurso) {
      this.lanzarModalInformativo('Acción Inválida', 'No hay datos del concurso disponibles para exportar.', 'error');
      return;
    }

    const nombreConcurso = this.concurso.nombre;
    const url = `http://localhost:8080/api/inscripciones/exportar-pdf-general?idConcurso=${this.idConcurso}&nombreConcurso=${encodeURIComponent(nombreConcurso)}`;

    this.http.post(url, {}, { responseType: 'blob' }).subscribe({
      next: (blob: Blob) => {
        const urlLocal = window.URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = urlLocal;
        enlace.download = `Listado_General_${nombreConcurso.replace(/\s+/g, '_')}.pdf`;
        document.body.appendChild(enlace);
        enlace.click();
        
        document.body.removeChild(enlace);
        window.URL.revokeObjectURL(urlLocal);

        this.registrarAuditoria(
          'DESCARGA_PDF_GENERAL',
          `El administrador ha descargado el PDF de control general para el concurso: ${nombreConcurso}.`
        );
        this.lanzarModalInformativo('Descarga Exitosa', 'El documento de control general se ha generado e iniciado correctamente.', 'success');
      },
      error: (err) => {
        console.error('Error al generar el PDF General:', err);
        this.lanzarModalInformativo('Error de Exportación', 'Hubo un problema al estructurar el archivo PDF en el servidor.', 'error');
      }
    });
  }

  generarPdfParticipantesSeleccionados(): void {
    if (!this.inscripcionesFiltradas || this.inscripcionesFiltradas.length === 0) return;

    const listaPrimitiva: number[] = [];
    for (const ins of this.inscripcionesFiltradas) {
      if (ins && ins.seleccionado === true) {
        const idVal = ins.idInscripcion ?? ins.id;
        if (idVal !== undefined && idVal !== null) {
          listaPrimitiva.push(Number(idVal));
        }
      }
    }

    if (listaPrimitiva.length === 0) {
      this.lanzarModalInformativo('Falta Selección', 'Por favor, selecciona primero al menos una agrupación utilizando las casillas de verificación.', 'error');
      return;
    }

    const cuerpoPeticion = Array.from(listaPrimitiva);
    const url = 'http://localhost:8080/api/inscripciones/exportar-pdf-seleccionados';

    this.http.post(url, cuerpoPeticion, { responseType: 'blob' }).subscribe({
      next: (blob: Blob) => {
        const urlLocal = window.URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = urlLocal;
        enlace.download = 'Fichas_Componentes_Seleccionados.pdf';
        document.body.appendChild(enlace);
        enlace.click();
        
        document.body.removeChild(enlace);
        window.URL.revokeObjectURL(urlLocal);

        this.registrarAuditoria(
          'DESCARGA_PDF_AGRUPACIONES_SELECCIONADAS',
          `El administrador ha descargado el PDF de control de las agrupaciones seleccionadas para el concurso: ${this.concurso?.nombre || 'N/A'}.`
        );
        this.lanzarModalInformativo('Fichas Listas', 'El lote de fichas de componentes seleccionados se ha descargado de forma idónea.', 'success');
      },
      error: (err) => {
        console.error('Error en la petición POST del PDF:', err);
        this.lanzarModalInformativo('Fallo de Compilación', 'Ocurrió un error inesperado al unificar las fichas seleccionadas.', 'error');
      }
    });
  }

  abrirModalCircular(): void {
    if (this.esHistorico) return; 
    this.circularAsunto = '';
    this.circularCuerpo = '';
    this.archivosCircularSeleccionados = [];
    this.enviandoCircular = false; 
    this.errorCircular = null;     
    this.mostrarModalCircular = true;
  }

  cerrarModalCircular(): void {
    if (this.enviandoCircular) return; 
    this.mostrarModalCircular = false;
  }

  prepararArchivoCircular(event: any): void {
    if (event.target.files && event.target.files.length > 0) {
      const nuevosArchivos = Array.from(event.target.files) as File[];
      
      nuevosArchivos.forEach((nuevo: File) => {
        const yaExiste = this.archivosCircularSeleccionados.some(
          f => f.name === nuevo.name && f.size === nuevo.size
        );
        if (!yaExiste) {
          this.archivosCircularSeleccionados.push(nuevo);
        }
      });
    }
  }

  eliminarArchivoDeCola(index: number): void {
    if (this.archivosCircularSeleccionados) {
      this.archivosCircularSeleccionados.splice(index, 1);
    }
  }

  procesarEnvioCircular(): void {
    if (this.esHistorico || !this.circularAsunto.trim() || !this.circularCuerpo.trim() || this.enviandoCircular) return;

    let idsAEnviar: number[] = [];
    if (this.tieneSeleccionados()) {
      idsAEnviar = this.inscripcionesFiltradas
        .filter(ins => ins.seleccionado === true)
        .map(ins => Number(ins.idInscripcion ?? ins.id));
    } else {
      idsAEnviar = this.inscripcionesFiltradas.map(ins => Number(ins.idInscripcion ?? ins.id));
    }

    if (idsAEnviar.length === 0) {
      this.errorCircular = 'No se han encontrado destinatarios válidos para realizar el envío.';
      return;
    }

    this.enviandoCircular = true;
    this.errorCircular = null;
    this.cdRef.detectChanges();

    const formData = new FormData();
    formData.append('asunto', this.circularAsunto.trim());
    formData.append('cuerpo', this.circularCuerpo.trim());
    formData.append('idsInscripciones', JSON.stringify(idsAEnviar)); 
    
    if (this.archivosCircularSeleccionados.length > 0) {
      this.archivosCircularSeleccionados.forEach((file: File) => {
        formData.append('archivo', file); 
      });
    }

    this.http.post('http://localhost:8080/api/concursos/enviar-circular', formData)
      .subscribe({
        next: (res: any) => {
          this.enviandoCircular = false;
          this.mostrarModalCircular = false; 
          
          const tipoEnvio = this.tieneSeleccionados() ? 'PARCIAL_MULTIPLE_ADJUNTO' : 'GLOBAL_MULTIPLE_ADJUNTO';
          this.registrarAuditoria(
            `CIRCULAR_INFORMATIVA_${tipoEnvio}`,
            `El administrador ha lanzado una circular con (${this.archivosCircularSeleccionados.length}) adjuntos para ${idsAEnviar.length} agrupaciones.`
          );

          this.lanzarModalInformativo(
            'Circular Desplegada', 
            `La circular informativa ha sido enviada correctamente por correo electrónico a los representantes de las ${idsAEnviar.length} agrupaciones asociadas.`, 
            'success'
          );
        },
        error: (err) => {
          this.enviandoCircular = false;
          console.error('Error al tramitar la circular multi-adjunto:', err);
          
          if (err.error && err.error.error) {
            this.errorCircular = 'Error del servidor: ' + err.error.error;
          } else if (err.status === 413) {
            this.errorCircular = 'Los archivos adjuntos son demasiado grandes para la pasarela de red.';
          } else {
            this.errorCircular = 'Hubo un problema al inicializar el envío de paquetes masivos.';
          }
          this.cdRef.detectChanges();
        }
      });
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
      next: () => console.log(`[Auditoría] Log guardado: ${accion}`),
      error: (err) => console.error('[Auditoría] Error al insertar log:', err)
    });
  }

}

