import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AgrupacionService } from './gestion-agrupaciones-rep.service';
import { Agrupacion } from './gestion-agrupaciones-rep.model';

@Component({
  selector: 'app-gestion-agrupaciones-rep',
  standalone: true,
  imports: [CommonModule, RouterModule], 
  templateUrl: './gestion-agrupaciones-rep.html',
  styleUrl: './gestion-agrupaciones-rep.css'
})
export class GestionAgrupacionesRepComponent implements OnInit {
  
  // Inicializamos como array vacío para evitar errores en el template
  agrupaciones: Agrupacion[] = [];
  loading: boolean = true; // Para saber si está cargando

  constructor(
    private agrupacionService: AgrupacionService,
    private cdr: ChangeDetectorRef // Inyectamos el detector de cambios
  ) {}

  ngOnInit(): void {
    this.obtenerDatosDeBD();
  }

  obtenerDatosDeBD() {
    const idRepresentante = 1; 
    this.loading = true;

    this.agrupacionService.getAgrupacionesPorRepresentante(idRepresentante).subscribe({
      next: (data) => {
        // Asignamos los datos recibidos
        this.agrupaciones = data;
        this.loading = false;
        
        console.log('Datos cargados correctamente:', this.agrupaciones);
        
        // Forzamos a Angular a que revise la vista tras el refresco
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        console.error('Error al conectar con el servidor:', err);
      }
    });
  }

  // Función de ayuda para el @for (mejora el rendimiento al refrescar)
  trackByAgrupacionId(index: number, agrup: Agrupacion): number {
    return agrup.idAgrupacion;
  }
}