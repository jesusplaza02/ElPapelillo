import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router'; // Importante para el ID dinámico

@Component({
  selector: 'app-documentacion-rep',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './documentacion-rep.html',
  styleUrls: ['./documentacion-rep.css']
})
export class DocumentacionRepComponent implements OnInit {
  
  listaDocs: any[] = [];
  idAgrupacionActual: string | null = null;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,    // Para leer el ID de la URL
    private cd: ChangeDetectorRef     // Para forzar el pintado de la lista
  ) { }

  ngOnInit(): void {
    // Obtenemos el ID de la ruta (el /:id que pusimos en el routing)
    this.idAgrupacionActual = this.route.snapshot.paramMap.get('id');
    
    if (this.idAgrupacionActual) {
      this.cargarDocs(this.idAgrupacionActual);
    }
  }

  cargarDocs(id: string) {
    // Usamos el ID dinámico en la URL de la petición
    this.http.get<any[]>(`http://localhost:8080/api/documentos/agrupacion/${id}`)
      .subscribe({
        next: (data) => {
          this.listaDocs = data;
          // Forzamos a Angular a detectar cambios para que pinte las cards
          this.cd.detectChanges(); 
        },
        error: (err) => console.error('Error al cargar documentos', err)
      });
  }

  getClaseEstado(estado: string): string {
    const clases: any = {
      'APROBADO': 'state-approved',
      'PENDIENTE': 'state-pending',
      'RECHAZADO': 'state-rejected'
    };
    return clases[estado] || '';
  }

  getTextoEstado(estado: string): string {
    const textos: any = {
      'APROBADO': 'Aprobado',
      'PENDIENTE': 'Pendiente revisión',
      'RECHAZADO': 'Rechazado'
    };
    return textos[estado] || estado;
  }

  // Método para descargar archivos gestionando el BLOB y el nombre
  descargarArchivo(url: string, nombreArchivo: string) {
    // Aseguramos que la URL apunte al puerto 8080 del backend
    const fullUrl = `http://localhost:8080/${url}`;
    
    this.http.get(fullUrl, { responseType: 'blob' })
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
        error: (err) => console.error('Error al descargar el archivo', err)
      });
  }

  // Métodos para la subida de archivos
  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file && this.idAgrupacionActual) {
      this.subirArchivo(file, this.idAgrupacionActual);
    }
  }

  subirArchivo(file: File, idAgrupacion: string) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('idAgrupacion', idAgrupacion);
    formData.append('nombreDoc', file.name); // Usamos el nombre real del archivo

    this.http.post('http://localhost:8080/api/documentos/upload', formData)
      .subscribe({
        next: (res) => {
          console.log('¡Documento guardado!', res);
          if (this.idAgrupacionActual) {
            this.cargarDocs(this.idAgrupacionActual);
          }
        },
        error: (err) => console.error('Error al subir', err)
      });
  }
}