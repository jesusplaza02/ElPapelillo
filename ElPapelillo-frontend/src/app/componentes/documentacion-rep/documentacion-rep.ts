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
  idInscripcionActual: string | null = null;
  
  // 🔑 CONTEXTO DE INSCRIPCIÓN: Declarado explícitamente para limpiar los errores del HTML
  inscripcionActiva: any = null; 
  
  // ID del usuario activo para auditorías (obtenido del localStorage)
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
    // 1. Capturamos el ID del usuario del almacenamiento local
    const idLogueado = localStorage.getItem('idUsuario');
    if (idLogueado) {
      this.usuarioIdActual = Number(idLogueado);
    }

    // 2. LIMPIEZA PREVENTIVA: Reseteamos los estados para evitar fugas visuales entre pantallas
    this.listaDocs = [];
    this.inscripcionActiva = null;
    this.loading = true;

    // 3. Capturamos el ID de la INSCRIPCIÓN desde la URL
    this.idInscripcionActual = this.route.snapshot.paramMap.get('id');
    if (this.idInscripcionActual) {
      this.cargarDocs(this.idInscripcionActual);
    } else {
      this.irAlPanel();
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
      // VALIDACIÓN: Solo PDF
      if (archivo.type !== 'application/pdf') {
        this.mensajeError = 'Error: Solo se permiten archivos en formato PDF.';
        this.resetearInput(event);
        return;
      }

      // VALIDACIÓN: Máximo 5MB
      const maxTamano = 5 * 1024 * 1024;
      if (archivo.size > maxTamano) {
        this.mensajeError = 'Error: El archivo supera el límite de 5MB.';
        this.resetearInput(event);
        return;
      }

      this.archivoSeleccionado = archivo;
      
      // Lógica de nombre automático adaptado a la Inscripción
      if (!this.nuevoDocNombre) {
        switch (this.nuevoDocTipo) {
          case 'DNI':
            this.nuevoDocNombre = `DNI_Ins_${this.idInscripcionActual}`;
            break;
          case 'REPERTORIO':
            this.nuevoDocNombre = `Letras_Ins_${this.idInscripcionActual}`;
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

    if (!this.archivoSeleccionado || !this.idInscripcionActual || !this.nuevoDocNombre) {
      this.mensajeError = 'Por favor, rellena todos los campos obligatorios.';
      return;
    }

    const formData = new FormData();
    formData.append('file', this.archivoSeleccionado);
    formData.append('idInscripcion', this.idInscripcionActual);
    formData.append('nombreDoc', this.nuevoDocNombre);
    formData.append('tipo', this.nuevoDocTipo);
    formData.append('usuarioId', this.usuarioIdActual.toString()); 

    this.http.post('http://localhost:8080/api/documentos/upload', formData)
      .subscribe({
        next: () => {
          alert('¡Archivo guardado con éxito!');
          this.mostrarForm = false;
          this.limpiarFormulario();
          this.cargarDocs(this.idInscripcionActual!);
        },
        error: (err) => {
          this.mensajeError = err.error?.error || 'Error técnico al subir el documento.';
          console.error('Error al subir', err);
        }
      });
  }

  // --- MÉTODOS DE DATOS ---
  cargarDocs(idInscripcion: string) {
    this.loading = true;
    this.listaDocs = []; 

    this.http.get<any[]>(`http://localhost:8080/api/documentos/inscripcion/${idInscripcion}`)
      .subscribe({
        next: (data) => {
          this.listaDocs = data ? data : [];
          
          // 🔑 ESTRATEGIA DE CONTEXTO FIJO:
          // Si la inscripción ya cuenta con documentos, extraemos el objeto inscripción del primer registro
          if (this.listaDocs.length > 0 && this.listaDocs[0].inscripcion) {
            this.inscripcionActiva = this.listaDocs[0].inscripcion;
          } else {
            // Si la inscripción está vacía (como tu ID 22), solicitamos los datos básicos de la inscripción al backend
            this.http.get<any>(`http://localhost:8080/api/inscripciones/${idInscripcion}`)
              .subscribe({
                next: (res) => {
                  this.inscripcionActiva = res;
                },
                error: (err) => {
                  console.error('No se pudo mapear el contexto de la inscripción vacía:', err);
                  // Backup de seguridad para que la plantilla HTML no rompa bajo ninguna circunstancia
                  this.inscripcionActiva = {
                    agrupacion: { nombre: 'Cargando Agrupación...' },
                    concurso: { nombre: 'Concurso Local' }
                  };
                }
              });
          }
          
          this.loading = false;
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error('Error al cargar documentos de la inscripción:', err);
          this.listaDocs = [];
          this.loading = false;
          this.cd.detectChanges();
        }
      });
  }

  descargarArchivo(url: string, nombreArchivo: string) {
    this.http.get(`http://localhost:8080/${url}`, { responseType: 'blob' })
      .subscribe({
        next: (blob) => {
          const urlBlob = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = urlBlob;
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