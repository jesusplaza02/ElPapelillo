import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, RouterLink } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms'; 
import { AgrupacionService } from './gestion-agrupaciones-rep.service';
import { Inscripcion, Agrupacion } from './gestion-agrupaciones-rep.model';

@Component({
  selector: 'app-gestion-agrupaciones-rep',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, RouterLink],
  templateUrl: './gestion-agrupaciones-rep.html',
  styleUrl: './gestion-agrupaciones-rep.css'
})
export class GestionAgrupacionesRepComponent implements OnInit {
  
  private agrupacionService = inject(AgrupacionService);
  private cdr = inject(ChangeDetectorRef);
  private router = inject(Router);

  inscripciones: Inscripcion[] = [];
  misAgrupacionesBase: any[] = []; 
  concursosActivos: any[] = [];
  
  concursoSeleccionado: any = null;
  agrupacionExistenteSeleccionada: any = null; 
  
  modoFormulario: 'NUEVA' | 'EXISTENTE' = 'NUEVA';
  anioCalculado: number | null = null;
  tipoDerivado: string = ''; 
  loading: boolean = true;
  mostrandoFormulario: boolean = false;

  // 🌟 VARIABLES DE CONTROL PARA VENTANAS MODALES INTEGRADAS
  mostrarModalExito: boolean = false;
  mostrarModalDuplicado: boolean = false;
  mensajeErrorDuplicado: string = '';

  ngOnInit(): void {
    const idLogueado = localStorage.getItem('idUsuario'); 
    if (idLogueado) {
      this.cargarDatos(Number(idLogueado));
    } else {
      this.router.navigate(['/login']);
    }
  }

  cargarDatos(idRep: number): void {
    this.loading = true;

    // 1. Cargar las Inscripciones para la tabla principal
    this.agrupacionService.getInscripcionesPorRepresentante(idRep).subscribe({
      next: (data) => {
        this.inscripciones = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar inscripciones:', err);
        this.loading = false;
      }
    });

    // 2. Cargar Agrupaciones Base para "Existente" (Filtrando duplicados)
    this.agrupacionService.getMisAgrupacionesBase(idRep).subscribe({
      next: (data: any[]) => {
        if (data && Array.isArray(data)) {
          const mapeoIds = new Set();
          
          this.misAgrupacionesBase = data.filter(ins => {
            const idAgrup = ins.agrupacion?.idAgrupacion;
            if (idAgrup && !mapeoIds.has(idAgrup)) {
              mapeoIds.add(idAgrup);
              return true; 
            }
            return false; 
          });
        } else {
          this.misAgrupacionesBase = [];
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar o filtrar agrupaciones base:', err);
      }
    });

    // 3. Cargar Concursos Activos para el desplegable
    this.agrupacionService.getConcursosActivos().subscribe({
      next: (data) => {
        this.concursosActivos = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar concursos activos:', err);
      }
    });
  }

  onConcursoChange(): void {
    if (this.concursoSeleccionado) {
      const fecha = new Date(this.concursoSeleccionado.fechaInicio);
      this.anioCalculado = fecha.getFullYear();
      this.tipoDerivado = this.concursoSeleccionado.tipoConcurso;
    } else {
      this.anioCalculado = null;
      this.tipoDerivado = '';
    }
  }

  toggleFormulario(estado: boolean): void {
    this.mostrandoFormulario = estado;
    if (!estado) this.resetearEstadoFormulario();
  }

  resetearEstadoFormulario(): void {
    this.concursoSeleccionado = null;
    this.agrupacionExistenteSeleccionada = null;
    this.anioCalculado = null;
    this.tipoDerivado = '';
    this.modoFormulario = 'NUEVA';
  }

  // 🌟 CIERRE DEL MODAL DE ÉXITO (Limpia formulario y refresca la rejilla)
  confirmarCierreModalExito(): void {
    this.mostrarModalExito = false;
    this.toggleFormulario(false); 
    
    const idLogueado = localStorage.getItem('idUsuario');
    if (idLogueado) {
      setTimeout(() => this.cargarDatos(Number(idLogueado)), 100);
    }
    this.cdr.detectChanges();
  }

  enviarFormulario(formulario: NgForm): void {
    const idLogueado = localStorage.getItem('idUsuario');
    if (!idLogueado || !this.concursoSeleccionado) return;

    // ======================================================================
    // 1. VALIDACIÓN INTERNA: Evitar duplicar la misma agrupación en el mismo concurso
    // ======================================================================
    if (this.modoFormulario === 'EXISTENTE' && this.agrupacionExistenteSeleccionada) {
      const yaInscrita = this.inscripciones.some(ins => 
        ins.concurso?.idConcurso === this.concursoSeleccionado.idConcurso &&
        ins.agrupacion?.idAgrupacion === this.agrupacionExistenteSeleccionada?.idAgrupacion
      );

      if (yaInscrita) {
        // Activamos modal interno de advertencia en vez de un alert nativo
        this.mensajeErrorDuplicado = `La agrupación "${this.agrupacionExistenteSeleccionada.nombre}" ya se encuentra inscrita en el concurso "${this.concursoSeleccionado.nombre}".`;
        this.mostrarModalDuplicado = true;
        this.cdr.detectChanges();
        return; 
      }
    }

    // ======================================================================
    // 2. CONSTRUCCIÓN DEL PAYLOAD PARA EL BACKEND
    // ======================================================================
    let payload: any = {
      concurso: { idConcurso: this.concursoSeleccionado.idConcurso }
    };

    if (this.modoFormulario === 'EXISTENTE') {
      if (!this.agrupacionExistenteSeleccionada) return;
      payload.agrupacion = { idAgrupacion: this.agrupacionExistenteSeleccionada.idAgrupacion };
      
    } else {
      if (!formulario.valid) return;
      const v = formulario.value;
      
      payload.agrupacion = {
        nombre: v.nombre,
        nombreUltimaParticipacion: v.nombreUltimaParticipacion,
        categoria: v.categoria, 
        anio: this.anioCalculado,
        tipoConcurso: this.tipoDerivado, 
        representante: { idUsuario: Number(idLogueado) }
      };

      if (this.tipoDerivado === 'CANTO') {
        payload.agrupacion.agrupacionCanto = {
          modalidad: v.modalidad, 
          autorLetra: v.autorLetra,
          autorMusica: v.autorMusica,
          direccion: v.direccion
        };

      } else if (this.tipoDerivado === 'DRAG') {
        if (!payload.agrupacion.categoria) payload.agrupacion.categoria = 'ADULTO';
        payload.agrupacion.agrupacionDrag = {
          nombreArtisticoDrag: v.nombreArtisticoDrag,
          disenador: v.disenador
        };

      } else if (this.tipoDerivado === 'DIOSES') {
        payload.agrupacion.categoria = v.modalidadDios; 
        payload.agrupacion.agrupacionDioses = {
          modalidadDios: v.modalidadDios,
          modelo: v.modelo,
          disenador: v.disenador
        };

      } else if (this.tipoDerivado === 'OTRO') {
        if (!payload.agrupacion.categoria) payload.agrupacion.categoria = 'OTRO';
        payload.agrupacion.agrupacionOtro = {
          comentariosDestacables: v.comentariosDestacables
        };
      }
    }

    // ======================================================================
    // 3. ENVÍO DE LA PETICIÓN AL SERVICIO
    // ======================================================================
    this.agrupacionService.crearInscripcion(payload).subscribe({
      next: () => {
        // Lanzamos modal estético propio e inicializamos el form
        this.mostrarModalExito = true;
        formulario.resetForm(); 
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al procesar la inscripción:', err);
        alert('Hubo un error en el servidor al guardar la inscripción.');
      }
    });
  }
}