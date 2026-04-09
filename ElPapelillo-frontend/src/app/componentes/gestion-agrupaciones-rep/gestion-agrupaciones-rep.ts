import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-gestion-agrupaciones-rep', // Nombre de la etiqueta HTML
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './gestion-agrupaciones-rep.html', 
  styleUrl: './gestion-agrupaciones-rep.css' 
})
export class GestionAgrupacionesRepComponent {
  
  agrupaciones = [
    { 
      id: 1, 
      nombre: 'Los Papelillos Reales', 
      tipo: 'Chirigota', 
      estado: 'Aprobada', 
      concurso: 'Málaga 2026', 
      anio: 2026, 
      fianzaEstado: 'Pagada' 
    },
    { 
      id: 2, 
      nombre: 'La Cantera', 
      tipo: 'Comparsa', 
      estado: 'Pendiente', 
      concurso: 'Málaga 2026', 
      anio: 2026, 
      fianzaEstado: 'Pendiente' 
    }
  ];

  constructor() {}

  // Aquí podrás añadir funciones más adelante, como borrar o editar
}