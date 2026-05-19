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
    fechaFin: ''
  };

  inscripciones: any[] = [];
  inscripcionesFiltradas: any[] = [];
  
  // Variables de control de filtros activas (Modalidad eliminada)
  filtroTexto: string = '';
  filtroCategoria: string = '';
  filtroEstado: string = '';
  filtroFianza: string = '';

  stats = {
    totalGrupos: 0,
    pendientesValidar: 0,
    fianzasPendientes: 0
  };

  ngOnInit(): void {
    this.idConcurso = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarDatosConcurso();
    this.cargarInscripciones();
  }

  cargarDatosConcurso(): void {
    this.http.get(`http://localhost:8080/api/concursos/${this.idConcurso}`).subscribe({
      next: (data: any) => {
        if (data) {
          this.concurso = data;
        } else {
          this.concurso = null;
        }
        this.cdRef.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar datos del concurso:', err);
        this.concurso = null;
        this.cdRef.detectChanges();
      }
    });
  }

  cargarInscripciones(): void {
    this.http.get<any[]>(`http://localhost:8080/api/inscripciones/concurso/${this.idConcurso}`).subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.inscripciones = data;
        } else {
          this.inscripciones = [];
        }
        this.finalizarCarga();
      },
      error: (err) => {
        console.error('El backend ha dado un error 500 para este concurso:', err);
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

  // Lógica de Filtrado Combinado Multi-Criterio (Sin Modalidad)
  filtrarAgrupaciones(): void {
    this.inscripcionesFiltradas = this.inscripciones.filter(ins => {
      
      // 1. Filtro por cuadro de texto (Busca por nombre de agrupación o representante)
      const cumpleTexto = !this.filtroTexto.trim() || 
        ins.agrupacion?.nombre?.toLowerCase().includes(this.filtroTexto.toLowerCase()) ||
        ins.agrupacion?.representante?.nombre?.toLowerCase().includes(this.filtroTexto.toLowerCase());

      // 2. Filtro por Categoría
      const cumpleCategoria = !this.filtroCategoria || 
        ins.agrupacion?.categoria?.toUpperCase() === this.filtroCategoria.toUpperCase();

      // 3. Filtro por Estado de la Inscripción
      const cumpleEstado = !this.filtroEstado || 
        ins.estadoInscripcion?.toUpperCase() === this.filtroEstado.toUpperCase();

      // 4. Filtro por Fianza (Controla booleanos true/false y strings de BD)
      let cumpleFianza = true;
      if (this.filtroFianza === 'PAGADA') {
        cumpleFianza = ins.fianza === true || ins.fianza === 'PAGADA' || ins.fianza === 'Pagada';
      } else if (this.filtroFianza === 'PENDIENTE') {
        cumpleFianza = !ins.fianza || ins.fianza === 'PENDIENTE' || ins.fianza === 'Pendiente' || ins.fianza === false;
      }

      // La inscripción pasa si cumple los 4 filtros simultáneamente
      return cumpleTexto && cumpleCategoria && cumpleEstado && cumpleFianza;
    });

    // Sincroniza los cambios con la vista al instante
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
      alert('No hay datos del concurso disponibles para exportar.');
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

        // === LLAMADA EXACTA A TU SISTEMA DE AUDITORÍA ===
        this.registrarAuditoria(
          'DESCARGA_PDF_GENERAL',
          `El administrador ha descargado el PDF de control general para el concurso: ${nombreConcurso}.`
        );
        // ===============================================
      },
      error: (err) => {
        console.error('Error al generar el PDF General en el frontend:', err);
        alert('Hubo un problema al procesar el archivo PDF general.');
      }
    });
  }


  generarPdfParticipantesSeleccionados(): void {
  if (!this.inscripcionesFiltradas || this.inscripcionesFiltradas.length === 0) {
    return;
  }

  // 1. Extraemos los números en caliente
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
    alert('Por favor, selecciona primero al menos una agrupación utilizando las casillas de verificación.');
    return;
  }

  // 2. OBLIGAMOS A ANGULAR A ENVIAR EL ARRAY PLANO (Sin envolturas de objetos)
  const cuerpoPeticion = Array.from(listaPrimitiva);

  const url = 'http://localhost:8080/api/inscripciones/exportar-pdf-seleccionados';

  // 3. Enviamos el cuerpo directamente
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
    },
    error: (err) => {
      console.error('Error en la petición POST del PDF:', err);
      alert('Ocurrió un error al intentar generar las fichas seleccionadas.');
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
      next: () => console.log(`[Auditoría] Registro guardado con éxito para el Admin ID (${administradorId}): ${accion}`),
      error: (err) => console.error('[Auditoría] Error al insertar en logauditoria:', err)
    });
  }
}