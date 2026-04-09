import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router'; // Importamos RouterModule para soportar [routerLink] con parámetros

@Component({
  selector: 'app-gestion-agrupaciones-rep',
  standalone: true,
  /* IMPORTANTE: Usamos RouterModule en lugar de solo RouterLink 
     para que las rutas con parámetros como agrup.id funcionen correctamente.
  */
  imports: [CommonModule, RouterModule],
  templateUrl: './gestion-agrupaciones-rep.html', 
  styleUrl: './gestion-agrupaciones-rep.css' 
})
export class GestionAgrupacionesRepComponent {
  
  // Datos de prueba para verificar que el @for y el CSS funcionan
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

  constructor() {
    
  }

  /* Futuras funciones:
     descargarRecibo(id: number) { ... }
     eliminarAgrupacion(id: number) { ... }
  */
}