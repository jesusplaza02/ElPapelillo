import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms'; // Vital para el formulario

@Component({
  selector: 'app-documentacion-rep',
  standalone: true,
  imports: [CommonModule, HttpClientModule, FormsModule],
  templateUrl: './documentacion-rep.html',
  styleUrls: ['./documentacion-rep.css']
})
export class DocumentacionRepComponent implements OnInit {
  
  listaDocs: any[] = [];
  idAgrupacionActual: string | null = null;

  // --- NUEVAS VARIABLES PARA EL FORMULARIO ---
  mostrarForm: boolean = false;
  nuevoDocNombre: string = '';
  nuevoDocTipo: string = 'OTROS';
  archivoSeleccionado: File | null = null;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.idAgrupacionActual = this.route.snapshot.paramMap.get('id');
    if (this.idAgrupacionActual) {
      this.cargarDocs(this.idAgrupacionActual);
    }
  }

  // --- NUEVOS MÉTODOS DEL FORMULARIO ---
  toggleFormulario() {
    this.mostrarForm = !this.mostrarForm;
    if (!this.mostrarForm) {
      this.limpiarFormulario();
    }
  }

  prepararArchivo(event: any) {
    this.archivoSeleccionado = event.target.files[0];
    // Si el usuario no ha escrito nada, sugerimos el nombre del archivo real
    if (!this.nuevoDocNombre && this.archivoSeleccionado) {
      this.nuevoDocNombre = this.archivoSeleccionado.name;
    }
  }

  confirmarSubida() {
    if (this.archivoSeleccionado && this.idAgrupacionActual) {
      const formData = new FormData();
      formData.append('file', this.archivoSeleccionado);
      formData.append('idAgrupacion', this.idAgrupacionActual);
      formData.append('nombreDoc', this.nuevoDocNombre);
      formData.append('tipo', this.nuevoDocTipo);

      this.http.post('http://localhost:8080/api/documentos/upload', formData)
        .subscribe({
          next: () => {
            console.log('¡Subida con éxito!');
            this.mostrarForm = false;
            this.cargarDocs(this.idAgrupacionActual!);
            this.limpiarFormulario();
          },
          error: (err) => console.error('Error al subir', err)
        });
    }
  }

  limpiarFormulario() {
    this.nuevoDocNombre = '';
    this.nuevoDocTipo = 'OTROS';
    this.archivoSeleccionado = null;
  }

  // --- MÉTODOS EXISTENTES ---
  cargarDocs(id: string) {
    this.http.get<any[]>(`http://localhost:8080/api/documentos/agrupacion/${id}`)
      .subscribe({
        next: (data) => {
          this.listaDocs = data;
          this.cd.detectChanges();
        },
        error: (err) => console.error('Error al cargar documentos', err)
      });
  }

  descargarArchivo(url: string, nombreArchivo: string) {
    this.http.get(`http://localhost:8080/${url}`, { responseType: 'blob' })
      .subscribe({
        next: (blob) => {
          const urlBlob = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = urlBlob;
          a.download = nombreArchivo;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          window.URL.revokeObjectURL(urlBlob);
        },
        error: (err) => console.error('Error al descargar', err)
      });
  }

  getClaseEstado(estado: string): string {
    const clases: any = { 'APROBADO': 'state-approved', 'PENDIENTE': 'state-pending', 'RECHAZADO': 'state-rejected' };
    return clases[estado] || '';
  }

  getTextoEstado(estado: string): string {
    const textos: any = { 'APROBADO': 'Aprobado', 'PENDIENTE': 'Pendiente revisión', 'RECHAZADO': 'Rechazado' };
    return textos[estado] || estado;
  }
}