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
  misAgrupacionesFiltradas: any[] = []; 
  
  concursosActivos: any[] = [];
  
  concursoSeleccionado: any = null;
  agrupacionExistenteSeleccionada: any = null; 
  
  modoFormulario: 'NUEVA' | 'EXISTENTE' = 'NUEVA';
  anioCalculado: number | null = null;
  tipoDerivado: string = ''; 
  loading: boolean = true;
  mostrandoFormulario: boolean = false;

  mostrarModalExito: boolean = false;
  mostrarModalDuplicado: boolean = false;
  mensajeErrorDuplicado: string = '';

  dropdownAbierto: boolean = false;
  dropdownAgrupacionesAbierto: boolean = false;

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

    // Carga de Inscripciones para la tabla principal
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

    // Carga de Agrupaciones Base para "Existente" (Filtrando duplicados de ID)
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
          
          this.misAgrupacionesFiltradas = [];
        } else {
          this.misAgrupacionesBase = [];
          this.misAgrupacionesFiltradas = [];
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar o filtrar agrupaciones base:', err);
      }
    });

    // CARGAR Y FILTRAR CONCURSOS (Excluyendo HISTORICO)
    this.agrupacionService.getConcursosActivos().subscribe({
      next: (data: any[]) => {
        if (data && Array.isArray(data)) {
          this.concursosActivos = data.filter(c => {
            const estado = c?.estadoConcurso ? String(c.estadoConcurso).trim().toUpperCase() : '';
            return estado !== 'HISTORICO';
          });
        } else {
          this.concursosActivos = [];
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar concursos activos:', err);
      }
    });
  }

  // Al cambiar el concurso, filtramos por tipo de concurso
  onConcursoChange(): void {
    // Reseteamos siempre la selección previa para evitar mezclas erróneas
    this.agrupacionExistenteSeleccionada = null; 
    this.dropdownAgrupacionesAbierto = false;

    if (this.concursoSeleccionado) {
      const fecha = new Date(this.concursoSeleccionado.fechaInicio);
      this.anioCalculado = fecha.getFullYear();
      this.tipoDerivado = this.concursoSeleccionado.tipoConcurso;

      // Filtrado estricto: la agrupación base debe pertenecer al mismo tipo que el concurso destino
      this.misAgrupacionesFiltradas = this.misAgrupacionesBase.filter(ins => {
        return ins.agrupacion?.tipoConcurso === this.tipoDerivado;
      });

    } else {
      this.anioCalculado = null;
      this.tipoDerivado = '';
      this.misAgrupacionesFiltradas = [];
    }
    this.cdr.detectChanges();
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
    this.misAgrupacionesFiltradas = [];
    this.dropdownAbierto = false;
    this.dropdownAgrupacionesAbierto = false;
    this.cdr.detectChanges();
  }

  seleccionarConcursoCustom(concurso: any): void {
    this.concursoSeleccionado = concurso;
    this.dropdownAbierto = false; // Cerramos el menú tras seleccionar
    this.onConcursoChange();       
  }

  seleccionarAgrupacionCustom(agrupacion: any): void {
    this.agrupacionExistenteSeleccionada = agrupacion;
    this.dropdownAgrupacionesAbierto = false; // Cerramos el menú tras seleccionar
    this.cdr.detectChanges();
  }

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

    if (this.modoFormulario === 'EXISTENTE' && this.agrupacionExistenteSeleccionada) {
      const yaInscrita = this.inscripciones.some(ins => 
        ins.concurso?.idConcurso === this.concursoSeleccionado.idConcurso &&
        ins.agrupacion?.idAgrupacion === this.agrupacionExistenteSeleccionada?.idAgrupacion
      );

      if (yaInscrita) {
        this.mensajeErrorDuplicado = `La agrupación "${this.agrupacionExistenteSeleccionada.nombre}" ya se encuentra inscrita en el concurso "${this.concursoSeleccionado.nombre}".`;
        this.mostrarModalDuplicado = true;
        this.cdr.detectChanges();
        return; 
      }
    }

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

    this.agrupacionService.crearInscripcion(payload).subscribe({
      next: () => {
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