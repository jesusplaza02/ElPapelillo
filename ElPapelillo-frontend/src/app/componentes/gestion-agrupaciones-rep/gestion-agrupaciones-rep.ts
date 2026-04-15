import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms'; 
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
  
  // Listado de agrupaciones existentes
  agrupaciones: Agrupacion[] = [];
  
  // Datos para el formulario de inscripción
  concursosActivos: any[] = [];
  concursoSeleccionado: any = null;
  
  // Estados de la vista
  loading: boolean = true;
  mostrandoFormulario: boolean = false;

  constructor(
    private agrupacionService: AgrupacionService,
    private cdr: ChangeDetectorRef 
  ) {}

  ngOnInit(): void {
    // Cargamos todo al iniciar (usamos ID 1 por defecto)
    this.cargarDatos(1);
  }

  cargarDatos(idRep: number) {
    this.loading = true;

    // 1. Cargamos las agrupaciones del representante para el listado
    this.agrupacionService.getAgrupacionesPorRepresentante(idRep).subscribe({
      next: (data) => {
        this.agrupaciones = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar agrupaciones:', err);
      }
    });

    // 2. Cargamos los concursos activos para el desplegable del formulario
    this.agrupacionService.getConcursosActivos().subscribe({
      next: (data) => {
        console.log('Datos recibidos de concursos:', data); // Debug para consola
        this.concursosActivos = data;
        this.loading = false;
        
        // Forzamos la detección de cambios para que el @for del HTML reaccione
        this.cdr.markForCheck();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar concursos activos:', err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * Muestra u oculta el formulario de inscripción
   * @param estado true para mostrar, false para ocultar
   */
  toggleFormulario(estado: boolean) {
    this.mostrandoFormulario = estado;
    if (!estado) {
      this.concursoSeleccionado = null; // Limpiamos selección al cerrar
    }
    this.cdr.detectChanges();
  }

  /**
   * Envía los datos del formulario al servicio
   */
  enviarFormulario(formulario: any) {
    if (formulario.valid && this.concursoSeleccionado) {
      // Construimos el objeto que espera el Backend
      const payload = {
        nombre: formulario.value.nombre,
        nombreUltimaParticipacion: formulario.value.nombreUltimaParticipacion,
        categoria: formulario.value.categoria,
        idConcurso: this.concursoSeleccionado.idConcurso,
        tipoConcurso: this.concursoSeleccionado.tipoConcurso,
        idRepresentante: 1, // ID estático por ahora
        estadoInscripcion: 'PENDIENTE'
      };

      this.agrupacionService.crearAgrupacion(payload).subscribe({
        next: () => {
          alert('¡Inscripción realizada con éxito!');
          this.toggleFormulario(false);
          this.cargarDatos(1); // Recargamos la lista para ver la nueva tarjeta
        },
        error: (err) => {
          console.error('Error al guardar la inscripción:', err);
          alert('Hubo un error al procesar la inscripción.');
        }
      });
    } else {
      alert('Por favor, rellena todos los campos obligatorios.');
    }
  }

  /**
   * Optimización para el renderizado del bucle de tarjetas
   */
  trackByAgrupacionId(index: number, agrup: Agrupacion): number {
    return agrup.idAgrupacion;
  }
}