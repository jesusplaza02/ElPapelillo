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
  
  agrupaciones: Agrupacion[] = [];
  concursosActivos: any[] = [];
  concursoSeleccionado: any = null;
  
  loading: boolean = true;
  mostrandoFormulario: boolean = false;

  constructor(
    private agrupacionService: AgrupacionService,
    private cdr: ChangeDetectorRef 
  ) {}

  ngOnInit(): void {
    this.cargarDatos(1); // Representante ID 1
  }

  cargarDatos(idRep: number) {
    this.loading = true;
    this.agrupacionService.getAgrupacionesPorRepresentante(idRep).subscribe({
      next: (data) => {
        this.agrupaciones = data;
        this.cdr.detectChanges();
      }
    });

    this.agrupacionService.getConcursosActivos().subscribe({
      next: (data) => {
        this.concursosActivos = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => this.loading = false
    });
  }

  // Cambia entre vista listado y vista formulario
  toggleFormulario(estado: boolean) {
    this.mostrandoFormulario = estado;
    if (!estado) this.concursoSeleccionado = null;
    this.cdr.detectChanges();
  }

  enviarFormulario(formulario: any) {
    if (formulario.valid && this.concursoSeleccionado) {
      const payload = {
        ...formulario.value,
        idConcurso: this.concursoSeleccionado.idConcurso,
        tipoConcurso: this.concursoSeleccionado.tipoConcurso,
        idRepresentante: 1, // Ajustar según tu lógica de usuario
        estadoInscripcion: 'PENDIENTE'
      };

      this.agrupacionService.crearAgrupacion(payload).subscribe({
        next: () => {
          alert('¡Inscripción enviada con éxito!');
          this.toggleFormulario(false);
          this.cargarDatos(1); // Refrescar lista
        },
        error: (err) => console.error('Error al guardar:', err)
      });
    }
  }

  trackByAgrupacionId(index: number, agrup: Agrupacion): number {
    return agrup.idAgrupacion;
  }
}