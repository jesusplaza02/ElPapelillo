import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router'; // 1. Añadimos Router aquí
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
  
  agrupaciones: Agrupacion[] = [];
  concursosActivos: any[] = [];
  concursoSeleccionado: any = null;
  anioCalculado: number | null = null;
  tipoDerivado: string = ''; 
  loading: boolean = true;
  mostrandoFormulario: boolean = false;

  constructor(
    private agrupacionService: AgrupacionService,
    private cdr: ChangeDetectorRef,
    private router: Router // 2. Inyectamos el Router aquí para que no de error
  ) {}

  ngOnInit(): void {
    const idLogueado = localStorage.getItem('idUsuario'); 

    if (idLogueado) {
      this.cargarDatos(Number(idLogueado));
    } else {
      // 3. Ahora esto ya no dará error
      this.router.navigate(['/login']);
    }
  }

  cargarDatos(idRep: number) {
    this.loading = true;
    this.agrupacionService.getAgrupacionesPorRepresentante(idRep).subscribe({
      next: (data) => {
        this.agrupaciones = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar agrupaciones:', err)
    });

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

  onConcursoChange() {
    if (this.concursoSeleccionado) {
      const fecha = new Date(this.concursoSeleccionado.fechaInicio);
      this.anioCalculado = fecha.getFullYear();
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
    // 4. Recuperamos el ID para el refresco posterior
    const idLogueado = localStorage.getItem('idUsuario');

    if (formulario.valid && this.concursoSeleccionado && idLogueado) {
      const payload = { /* ... tus datos ... */ };

      this.agrupacionService.crearAgrupacion(payload).subscribe({
        next: () => {
          alert('¡Inscripción realizada!');
          this.toggleFormulario(false);
          
          setTimeout(() => {
            // 5. Usamos el ID recuperado, no el "1" fijo
            this.cargarDatos(Number(idLogueado));
          }, 0);
        },
        error: (err) => console.error('Error:', err)
      });
    }
  }

  trackByAgrupacionId(index: number, agrup: Agrupacion): number {
    return agrup.idAgrupacion;
  }
}