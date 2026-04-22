import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

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
  
  // ID del usuario que realiza la acción (para la tabla registroactividad)
  // En un entorno real, este ID se obtendría del login.
  usuarioIdActual: number = 1; 

  // --- VARIABLES DE ESTADO Y FORMULARIO ---
  loading: boolean = true;
  mostrarForm: boolean = false;
  nuevoDocNombre: string = '';
  nuevoDocTipo: string = 'OTROS'; 
  archivoSeleccionado: File | null = null;
  mensajeError: string | null = null; 

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private cd: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    // Capturamos el ID de la agrupación desde la URL
    this.idAgrupacionActual = this.route.snapshot.paramMap.get('id');
    if (this.idAgrupacionActual) {
      this.cargarDocs(this.idAgrupacionActual);
    }
  }

  // --- NAVEGACIÓN ---
  irAlPanel() {
    this.router.navigate(['/panel-representante']);
  }

  // --- GESTIÓN DEL FORMULARIO ---
  toggleFormulario() {
    this.mostrarForm = !this.mostrarForm;
    this.mensajeError = null; 
    if (!this.mostrarForm) {
      this.limpiarFormulario();
    }
  }

  limpiarFormulario() {
    this.nuevoDocNombre = '';
    this.nuevoDocTipo = 'OTROS';
    this.archivoSeleccionado = null;
    this.mensajeError = null;
  }

  private resetearInput(event: any) {
    event.target.value = '';
    this.archivoSeleccionado = null;
  }

  // --- MÉTODOS DE ARCHIVOS Y VALIDACIÓN ---
  prepararArchivo(event: any) {
    this.mensajeError = null; 
    const archivo = event.target.files[0];

    if (archivo) {
      // VALIDACIÓN RF21: Solo PDF
      if (archivo.type !== 'application/pdf') {
        this.mensajeError = 'Error: Solo se permiten archivos en formato PDF.';
        this.resetearInput(event);
        return;
      }

      // VALIDACIÓN RF21: Máximo 5MB
      const maxTamano = 5 * 1024 * 1024;
      if (archivo.size > maxTamano) {
        this.mensajeError = 'Error: El archivo supera el límite de 5MB.';
        this.resetearInput(event);
        return;
      }

      this.archivoSeleccionado = archivo;
      
      // Lógica de nombre automático según el tipo (RF11)
      if (!this.nuevoDocNombre) {
        switch (this.nuevoDocTipo) {
          case 'DNI':
            this.nuevoDocNombre = `DNI_Rep_${this.idAgrupacionActual}`;
            break;
          case 'REPERTORIO':
            this.nuevoDocNombre = `Letras_${this.idAgrupacionActual}`;
            break;
          default:
            this.nuevoDocNombre = archivo.name.replace(/\.[^/.]+$/, "");
            break;
        }
      }
    }
  }

  confirmarSubida() {
    this.mensajeError = null;

    if (!this.archivoSeleccionado || !this.idAgrupacionActual || !this.nuevoDocNombre) {
      this.mensajeError = 'Por favor, rellena todos los campos obligatorios.';
      return;
    }

    const formData = new FormData();
    formData.append('file', this.archivoSeleccionado);
    formData.append('idAgrupacion', this.idAgrupacionActual);
    formData.append('nombreDoc', this.nuevoDocNombre);
    formData.append('tipo', this.nuevoDocTipo);
    // Enviamos el usuarioId para el registro de auditoría en el backend (RF22)
    formData.append('usuarioId', this.usuarioIdActual.toString()); 

    this.http.post('http://localhost:8080/api/documentos/upload', formData)
      .subscribe({
        next: () => {
          alert('¡Archivo guardado con éxito!');
          this.mostrarForm = false;
          this.limpiarFormulario();
          this.cargarDocs(this.idAgrupacionActual!);
        },
        error: (err) => {
          // Captura el error de validación del backend (RF21)
          this.mensajeError = err.error?.error || 'Error técnico al subir el documento.';
          console.error('Error al subir', err);
        }
      });
  }

  // --- MÉTODOS DE DATOS ---
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
    // La URL llega desde el backend como "archivos/nombre_archivo.pdf"
    this.http.get(`http://localhost:8080/${url}`, { responseType: 'blob' })
      .subscribe({
        next: (blob) => {
          const urlBlob = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = urlBlob;
          // Forzamos extensión pdf en el nombre de descarga
          a.download = nombreArchivo.toLowerCase().endsWith('.pdf') ? nombreArchivo : `${nombreArchivo}.pdf`;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          window.URL.revokeObjectURL(urlBlob);
        },
        error: (err) => {
          console.error('Error en descarga:', err);
          alert('No se pudo descargar el archivo.');
        }
      });
  }

  // --- LÓGICA DE TRADUCCIÓN DE INTERFAZ (UI) ---
  getClaseEstado(estado: string): string {
    const clases: { [key: string]: string } = { 
      'APROBADO': 'state-approved', 
      'PENDIENTE': 'state-pending', 
      'RECHAZADO': 'state-rejected' 
    };
    return clases[estado] || 'state-pending';
  }

  getTextoEstado(estado: string): string {
    const textos: { [key: string]: string } = { 
      'APROBADO': 'Aprobado', 
      'PENDIENTE': 'Pendiente revisión', 
      'RECHAZADO': 'Rechazado' 
    };
    return textos[estado] || estado;
  }
}