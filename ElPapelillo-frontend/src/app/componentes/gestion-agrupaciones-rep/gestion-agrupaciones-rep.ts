import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms'; 
import { AgrupacionService } from './gestion-agrupaciones-rep.service';
import { Agrupacion } from './gestion-agrupaciones-rep.model';

@Component({
  selector: 'app-gestion-agrupaciones-rep',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './gestion-agrupaciones-rep.html',
  styleUrl: './gestion-agrupaciones-rep.css'
})
export class GestionAgrupacionesRepComponent implements OnInit {
  
  // Listado de agrupaciones para la tabla/cards
  agrupaciones: Agrupacion[] = [];
  
  // Datos para el formulario
  concursosActivos: any[] = [];
  concursoSeleccionado: any = null;
  
  // Variables calculadas automáticamente
  anioCalculado: number | null = null;
  tipoDerivado: string = ''; 

  // Estados de la interfaz
  loading: boolean = true;
  mostrandoFormulario: boolean = false;

  constructor(
    private agrupacionService: AgrupacionService,
    private cdr: ChangeDetectorRef 
  ) {}

  ngOnInit(): void {
    // Cargamos los datos iniciales (usando idRepresentante 1 por defecto)
    this.cargarDatos(1);
  }

  cargarDatos(idRep: number) {
    this.loading = true;

    // 1. Obtener agrupaciones del representante
    this.agrupacionService.getAgrupacionesPorRepresentante(idRep).subscribe({
      next: (data) => {
        this.agrupaciones = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar agrupaciones:', err)
    });

    // 2. Obtener concursos activos para el select
    this.agrupacionService.getConcursosActivos().subscribe({
      next: (data) => {
        this.concursosActivos = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar concursos activos:', err);
        this.loading = false;
      }
    });
  }

  /**
   * Se dispara automáticamente cuando el usuario cambia el concurso en el select.
   * Calcula el año y el tipo de formulario a mostrar.
   */
  onConcursoChange() {
    if (this.concursoSeleccionado) {
      // Extraer año de la fecha de inicio del concurso
      const fecha = new Date(this.concursoSeleccionado.fechaInicio);
      this.anioCalculado = fecha.getFullYear();

      // Determinar el tipo (CANTO, DRAG, DIOSES...) del concurso
      this.tipoDerivado = this.concursoSeleccionado.tipoConcurso;
    } else {
      this.anioCalculado = null;
      this.tipoDerivado = '';
    }
    this.cdr.detectChanges();
  }

  toggleFormulario(estado: boolean) {
    this.mostrandoFormulario = estado;
    if (!estado) {
      this.concursoSeleccionado = null;
      this.anioCalculado = null;
      this.tipoDerivado = '';
    }
    this.cdr.detectChanges();
  }

 enviarFormulario(formulario: NgForm) {
  if (formulario.valid && this.concursoSeleccionado) {
    const payload = { /* ... tus datos ... */ };

    this.agrupacionService.crearAgrupacion(payload).subscribe({
      next: () => {
        alert('¡Inscripción realizada!');
        
        // 1. Primero cerramos el formulario
        this.toggleFormulario(false);
        
        // 2. Usamos setTimeout para que Angular descanse un milisegundo 
        // antes de recargar la lista y evitar el error NG0100
        setTimeout(() => {
          this.cargarDatos(1);
        }, 0);
      },
      error: (err) => console.error('Error:', err)
    });
  }
}

  /**
   * Optimización para el renderizado del @for en el listado
   */
  trackByAgrupacionId(index: number, agrup: Agrupacion): number {
    return agrup.idAgrupacion;
  }
}