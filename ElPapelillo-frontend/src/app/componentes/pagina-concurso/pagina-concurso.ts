import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
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

  generarPDF(): void {
    console.log('Generando listado general en PDF...');
    alert('Función de exportación PDF en desarrollo para el TFG');
  }

  verDetalleRepresentante(representante: string): void {
    console.log('Viendo perfil de:', representante);
  }
}