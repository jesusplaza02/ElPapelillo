import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'detalle-agrupacion',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pagina-agrupacion.html',
  styleUrls: ['./pagina-agrupacion.css']
})
export class DetalleAgrupacionComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  private cdRef = inject(ChangeDetectorRef);

  idInscripcion!: number;
  inscripcion: any = null; // <-- Ahora el objeto principal es la inscripción
  participantes: any[] = []; 

  ngOnInit(): void {
    // Capturamos el ID de la inscripción que viene por la URL
    this.idInscripcion = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarDetalleInscripcion();
  }

  cargarDetalleInscripcion(): void {
    // Llamamos al endpoint de inscripciones (ajusta la URL exacta de tu API si cambia)
    this.http.get(`http://localhost:8080/api/inscripciones/${this.idInscripcion}`).subscribe({
      next: (data: any) => {
        this.inscripcion = data;
        // Los participantes suelen colgar de la agrupación que está dentro de la inscripción
        this.participantes = data?.agrupacion?.participantes || [];
        this.cdRef.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar el detalle de la inscripción:', err);
        this.cdRef.detectChanges();
      }
    });
  }

  actualizarEstadoInscripcion(nuevoEstado: 'APROBADO' | 'RECHAZADO'): void {
    if (!this.idInscripcion) return;

    this.http.put(`http://localhost:8080/api/inscripciones/${this.idInscripcion}/estado`, { estado: nuevoEstado }).subscribe({
      next: () => {
        alert(`Inscripción ${nuevoEstado === 'APROBADO' ? 'aceptada' : 'rechazada'} correctamente.`);
        this.cargarDetalleInscripcion();
      },
      error: (err) => {
        console.error('Error al actualizar el estado:', err);
      }
    });
  }

  descargarPDF(): void {
    alert('Descargando el listado de componentes en PDF...');
  }

  volver(): void {
    this.router.navigate(['/panel-control-administrador']); 
  }
}