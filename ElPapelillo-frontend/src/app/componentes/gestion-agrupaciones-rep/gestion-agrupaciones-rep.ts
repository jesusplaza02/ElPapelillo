import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms'; 
import { AgrupacionService } from './gestion-agrupaciones-rep.service';
import { Inscripcion, Agrupacion } from './gestion-agrupaciones-rep.model';

@Component({
  selector: 'app-gestion-agrupaciones-rep',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './gestion-agrupaciones-rep.html',
  styleUrl: './gestion-agrupaciones-rep.css'
})
export class GestionAgrupacionesRepComponent implements OnInit {
  
  inscripciones: Inscripcion[] = [];
  misAgrupacionesBase: Agrupacion[] = [];
  concursosActivos: any[] = [];
  
  concursoSeleccionado: any = null;
  agrupacionExistenteSeleccionada: Agrupacion | null = null;
  
  modoFormulario: 'NUEVA' | 'EXISTENTE' = 'NUEVA';
  anioCalculado: number | null = null;
  tipoDerivado: string = ''; 
  loading: boolean = true;
  mostrandoFormulario: boolean = false;

  constructor(
    private agrupacionService: AgrupacionService,
    private cdr: ChangeDetectorRef,
    private router: Router 
  ) {}

  ngOnInit(): void {
    const idLogueado = localStorage.getItem('idUsuario'); 
    if (idLogueado) {
      this.cargarDatos(Number(idLogueado));
    } else {
      this.router.navigate(['/login']);
    }
  }

  cargarDatos(idRep: number) {
    this.loading = true;

    // 1. Cargar las Inscripciones
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

    // 2. Cargar Agrupaciones Base para "Existente"
    this.agrupacionService.getMisAgrupacionesBase(idRep).subscribe({
      next: (data) => this.misAgrupacionesBase = data
    });

    // 3. Cargar Concursos Activos
    this.agrupacionService.getConcursosActivos().subscribe({
      next: (data) => this.concursosActivos = data
    });
  }

  onConcursoChange() {
    if (this.concursoSeleccionado) {
      const fecha = new Date(this.concursoSeleccionado.fechaInicio);
      this.anioCalculado = fecha.getFullYear();
      this.tipoDerivado = this.concursoSeleccionado.tipoConcurso;
    } else {
      this.anioCalculado = null;
      this.tipoDerivado = '';
    }
  }

  toggleFormulario(estado: boolean) {
    this.mostrandoFormulario = estado;
    if (!estado) this.resetearEstadoFormulario();
  }

  resetearEstadoFormulario() {
    this.concursoSeleccionado = null;
    this.agrupacionExistenteSeleccionada = null;
    this.anioCalculado = null;
    this.tipoDerivado = '';
    this.modoFormulario = 'NUEVA';
  }

  enviarFormulario(formulario: NgForm) {
    const idLogueado = localStorage.getItem('idUsuario');
    if (!idLogueado || !this.concursoSeleccionado) return;

    let payload: any = {
      concurso: { idConcurso: this.concursoSeleccionado.idConcurso }
    };

    if (this.modoFormulario === 'EXISTENTE') {
      if (!this.agrupacionExistenteSeleccionada) return;
      // Solo enlazamos el ID de la agrupación que ya existe en la BD
      payload.agrupacion = { idAgrupacion: this.agrupacionExistenteSeleccionada.idAgrupacion };
      
    } else {
      if (!formulario.valid) return;
      const v = formulario.value;
      
      // Creamos la agrupación y le inyectamos el ID del representante
      payload.agrupacion = {
        nombre: v.nombre,
        nombreUltimaParticipacion: v.nombreUltimaParticipacion,
        categoria: v.categoria,
        tipo: this.tipoDerivado,
        representante: { idUsuario: Number(idLogueado) } // ¡Perfecto!
      };

      if (this.tipoDerivado === 'CANTO') {
        payload.agrupacion.agrupacionCanto = {
          autorLetra: v.autorLetra,
          autorMusica: v.autorMusica,
          direccion: v.direccion
        };
      } else if (this.tipoDerivado === 'DRAG') {
        payload.agrupacion.agrupacionDrag = {
          nombreArtisticoDrag: v.nombreArtisticoDrag,
          disenador: v.disenador
        };
      } else if (this.tipoDerivado === 'DIOSES') {
        payload.agrupacion.agrupacionDioses = {
          modelo: v.modelo,
          disenador: v.disenador
        };
      }
    }

    // Enviamos a /api/inscripciones
    this.agrupacionService.crearInscripcion(payload).subscribe({
      next: () => {
        alert('¡Inscripción realizada con éxito!');
        this.toggleFormulario(false);
        formulario.resetForm();
        setTimeout(() => this.cargarDatos(Number(idLogueado)), 200);
      },
      error: (err) => {
        console.error(err);
        alert('Error al procesar la inscripción.');
      }
    });
  }
}