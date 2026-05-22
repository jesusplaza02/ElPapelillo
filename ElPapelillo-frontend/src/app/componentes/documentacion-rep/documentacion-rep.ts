import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
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
  
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cd = inject(ChangeDetectorRef);

  listaDocs: any[] = [];
  idInscripcionActual: string | null = null;
  inscripcionActiva: any = null; 
  usuarioIdActual: number = 1; 

  // --- VARIABLES DE ESTADO Y FORMULARIO ---
  loading: boolean = true;
  mostrarForm: boolean = false;
  nuevoDocNombre: string = '';
  nuevoDocTipo: string = 'OTROS'; 
  archivoSeleccionado: File | null = null;
  mensajeError: string | null = null; 

  // 🌟 VARIABLES NUEVAS PARA LAS VENTANAS MODALES INTEGRADAS
  mostrarModalExito: boolean = false;
  mostrarModalError: boolean = false;
  tituloModalError: string = '';
  contenidoModalError: string = '';

  ngOnInit(): void {
    const idLogueado = localStorage.getItem('idUsuario');
    if (idLogueado) {
      this.usuarioIdActual = Number(idLogueado);
    }

    this.listaDocs = [];
    this.inscripcionActiva = null;
    this.loading = true;

    this.idInscripcionActual = this.route.snapshot.paramMap.get('id');
    if (this.idInscripcionActual) {
      this.cargarDocs(this.idInscripcionActual);
    } else {
      this.irAlPanel();
    }
  }

  irAlPanel() {
    this.router.navigate(['/panel-representante']);
  }

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
    if (event && event.target) {
      event.target.value = '';
    }
    this.archivoSeleccionado = null;
  }

  // --- MÉTODOS DE ARCHIVOS Y VALIDACIÓN ---
  prepararArchivo(event: any) {
    this.mensajeError = null; 
    const archivo = event.target.files[0];

    if (archivo) {
      // 🌟 CORRECCIÓN: Modal integrado si no es PDF
      if (archivo.type !== 'application/pdf') {
        this.tituloModalError = 'Formato no válido';
        this.contenidoModalError = 'El sistema de validación de ElPapelillo únicamente acepta archivos con extensión .pdf para asegurar su correcta lectura administrativa.';
        this.mostrarModalError = true;
        this.resetearInput(event);
        this.cd.detectChanges();
        return;
      }

      // 🌟 CORRECCIÓN: Modal integrado si supera 5MB
      const maxTamano = 5 * 1024 * 1024;
      if (archivo.size > maxTamano) {
        this.tituloModalError = 'Tamaño de archivo excedido';
        this.contenidoModalError = 'El documento que intentas adjuntar supera el límite máximo permitido de 5 megabytes (5MB). Comprime el PDF e inténtalo de nuevo.';
        this.mostrarModalError = true;
        this.resetearInput(event);
        this.cd.detectChanges();
        return;
      }

      this.archivoSeleccionado = archivo;
      
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
      this.mensajeError = 'Por favor, rellena todos los campos obligatorios antes de subir.';
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
          // 🌟 CORRECCIÓN: Cambiado el alert nativo por ventana modal integrada de éxito
          this.mostrarModalExito = true;
          this.mostrarForm = false;
          this.limpiarFormulario();
          this.cd.detectChanges();
        },
        error: (err) => {
          // 🌟 CORRECCIÓN: Cambiado el error suelto por ventana modal de fallo de red/servidor
          this.tituloModalError = 'Fallo en la carga';
          this.contenidoModalError = err.error?.error || 'No se ha podido establecer comunicación con el servidor de archivos o el registro se encuentra duplicado.';
          this.mostrarModalError = true;
          this.cd.detectChanges();
        }
      });
  }

  // 🌟 MÉTODO DE CIERRE CONTROLADO (Limpia y refresca la rejilla al dar al botón)
  cerrarModalExitoYRefrescar() {
    this.mostrarModalExito = false;
    if (this.idInscripcionActual) {
      this.cargarDocs(this.idInscripcionActual);
    }
    this.cd.detectChanges();
  }

  cargarDocs(idInscripcion: string) {
    this.loading = true;
    this.listaDocs = []; 

    this.http.get<any[]>(`http://localhost:8080/api/documentos/inscripcion/${idInscripcion}`)
      .subscribe({
        next: (data) => {
          this.listaDocs = data ? data : [];
          
          if (this.listaDocs.length > 0 && this.listaDocs[0].inscripcion) {
            this.inscripcionActiva = this.listaDocs[0].inscripcion;
          } else {
            this.http.get<any>(`http://localhost:8080/api/inscripciones/${idInscripcion}`)
              .subscribe({
                next: (res) => {
                  this.inscripcionActiva = res;
                },
                error: (err) => {
                  console.error('No se pudo mapear el contexto:', err);
                  this.inscripcionActiva = {
                    agrupacion: { nombre: 'Gestión Documental' },
                    concurso: { nombre: 'Expediente' }
                  };
                }
              });
          }
          this.loading = false;
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error('Error al cargar documentos:', err);
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
          this.tituloModalError = 'Descarga no disponible';
          this.contenidoModalError = 'El archivo físico solicitado no se encuentra en la ruta del servidor o careces de los permisos de lectura necesarios.';
          this.mostrarModalError = true;
          this.cd.detectChanges();
        }
      });
  }

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