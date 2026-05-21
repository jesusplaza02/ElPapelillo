import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, RouterLink } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms'; 
import { AgrupacionService } from './gestion-agrupaciones-rep.service';
import { Inscripcion, Agrupacion } from './gestion-agrupaciones-rep.model';

@Component({
  selector: 'app-gestion-agrupaciones-rep',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, RouterLink],
  templateUrl: './gestion-agrupaciones-rep.html',
  styleUrl: './gestion-agrupaciones-rep.css'
})
export class GestionAgrupacionesRepComponent implements OnInit {
  
  inscripciones: Inscripcion[] = [];
  // CAMBIO CLAVE 1: Lo cambiamos a any[] o Inscripcion[] porque el backend manda objetos Inscripcion
  misAgrupacionesBase: any[] = []; 
  concursosActivos: any[] = [];
  
  concursoSeleccionado: any = null;
  // CAMBIO CLAVE 2: Guardará el objeto agrupación de la inscripción seleccionada
  agrupacionExistenteSeleccionada: any = null; 
  
  modoFormulario: 'NUEVA' | 'EXISTENTE' = 'NUEVA';
  anioCalculado: number | null = null;
  tipoDerivado: string = ''; 
  loading: boolean = true;
  mostrandoFormulario: boolean = false;

  constructor(
    private agrupacionService: AgrupacionService,
    private cdr: ChangeDetectorRef,
    private router: Router 
  ) {}

  ngOnInit(): void {
    const idLogueado = localStorage.getItem('idUsuario'); 
    if (idLogueado) {
      this.cargarDatos(Number(idLogueado));
    } else {
      this.router.navigate(['/login']);
    }
  }

  cargarDatos(idRep: number) {
    this.loading = true;

    // 1. Cargar las Inscripciones para la tabla principal
    this.agrupacionService.getInscripcionesPorRepresentante(idRep).subscribe({
      next: (data) => {
        this.inscripciones = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar inscripciones:', err);
        this.loading = false;
      }
    });

    // 2. Cargar Agrupaciones Base para "Existente" (Filtrando duplicados del historial de inscripciones)
    this.agrupacionService.getMisAgrupacionesBase(idRep).subscribe({
      next: (data: any[]) => {
        if (data && Array.isArray(data)) {
          const mapeoIds = new Set();
          
          // Filtramos el array dejando solo la primera aparición de cada idAgrupacion
          this.misAgrupacionesBase = data.filter(ins => {
            const idAgrup = ins.agrupacion?.idAgrupacion;
            if (idAgrup && !mapeoIds.has(idAgrup)) {
              mapeoIds.add(idAgrup);
              return true; // Nos quedamos con esta inscripción porque su agrupación aparece por primera vez
            }
            return false; // Saltamos las siguientes inscripciones de la misma agrupación
          });
        } else {
          this.misAgrupacionesBase = [];
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar o filtrar agrupaciones base:', err);
      }
    });

    // 3. Cargar Concursos Activos para el primer desplegable
    this.agrupacionService.getConcursosActivos().subscribe({
      next: (data) => {
        this.concursosActivos = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar concursos activos:', err);
      }
    });
  }

  onConcursoChange() {
    if (this.concursoSeleccionado) {
      const fecha = new Date(this.concursoSeleccionado.fechaInicio);
      this.anioCalculado = fecha.getFullYear();
      this.tipoDerivado = this.concursoSeleccionado.tipoConcurso;
    } else {
      this.anioCalculado = null;
      this.tipoDerivado = '';
    }
  }

  toggleFormulario(estado: boolean) {
    this.mostrandoFormulario = estado;
    if (!estado) this.resetearEstadoFormulario();
  }

  resetearEstadoFormulario() {
    this.concursoSeleccionado = null;
    this.agrupacionExistenteSeleccionada = null;
    this.anioCalculado = null;
    this.tipoDerivado = '';
    this.modoFormulario = 'NUEVA';
  }

  enviarFormulario(formulario: NgForm) {
    const idLogueado = localStorage.getItem('idUsuario');
    if (!idLogueado || !this.concursoSeleccionado) return;

    // ======================================================================
    // 1. VALIDACIÓN: Evitar duplicar la misma agrupación en el mismo concurso
    // ======================================================================
    if (this.modoFormulario === 'EXISTENTE' && this.agrupacionExistenteSeleccionada) {
      const yaInscrita = this.inscripciones.some(ins => 
        ins.concurso?.idConcurso === this.concursoSeleccionado.idConcurso &&
        ins.agrupacion?.idAgrupacion === this.agrupacionExistenteSeleccionada?.idAgrupacion
      );

      if (yaInscrita) {
        alert(`La agrupación "${this.agrupacionExistenteSeleccionada.nombre}" ya se encuentra inscrita en el concurso "${this.concursoSeleccionado.nombre}".`);
        return; // Detiene la ejecución aquí
      }
    }

    // ======================================================================
    // 2. CONSTRUCCIÓN DEL PAYLOAD PARA EL BACKEND
    // ======================================================================
    let payload: any = {
      concurso: { idConcurso: this.concursoSeleccionado.idConcurso }
    };

    // --- ESCENARIO A: REUTILIZAR AGRUPACIÓN EXISTENTE ---
    if (this.modoFormulario === 'EXISTENTE') {
      if (!this.agrupacionExistenteSeleccionada) {
        alert('Por favor, selecciona una agrupación válida.');
        return;
      }
      // Mandamos solo el ID de la fila que ya existe en la BD
      payload.agrupacion = { idAgrupacion: this.agrupacionExistenteSeleccionada.idAgrupacion };
      
    // --- ESCENARIO B: CREAR NUEVA AGRUPACIÓN DESDE CERO ---
    } else {
      if (!formulario.valid) return;
      const v = formulario.value;
      
      // Objeto base de la Agrupación (Campos de la tabla Madre 'agrupacion')
      payload.agrupacion = {
        nombre: v.nombre,
        nombreUltimaParticipacion: v.nombreUltimaParticipacion,
        categoria: v.categoria, // Salva correctamente INFANTIL, JUVENIL, ADULTO...
        anio: this.anioCalculado,
        tipoConcurso: this.tipoDerivado, // 'CANTO', 'DRAG', 'DIOSES', 'OTRO'
        representante: { idUsuario: Number(idLogueado) }
      };

      // Estructuración de los datos específicos de las subclases (Tablas Hijas)
      if (this.tipoDerivado === 'CANTO') {
        payload.agrupacion.agrupacionCanto = {
          modalidad: v.modalidad, // 'ROMANCERO', 'MURGA', 'COMPARSA'...
          autorLetra: v.autorLetra,
          autorMusica: v.autorMusica,
          direccion: v.direccion
        };

      } else if (this.tipoDerivado === 'DRAG') {
        // Si no se definió categoría arriba, aseguramos un valor por defecto
        if (!payload.agrupacion.categoria) {
          payload.agrupacion.categoria = 'ADULTO';
        }
        
        payload.agrupacion.agrupacionDrag = {
          nombreArtisticoDrag: v.nombreArtisticoDrag,
          disenador: v.disenador
        };

      } else if (this.tipoDerivado === 'DIOSES') {
        // En Dioses mapeamos la opción (DIOS/DIOSA) tanto en la madre como en la hija
        payload.agrupacion.categoria = v.modalidadDios; 
        
        payload.agrupacion.agrupacionDioses = {
          modalidadDios: v.modalidadDios,
          modelo: v.modelo,
          disenador: v.disenador
        };

      } else if (this.tipoDerivado === 'OTRO') {
        if (!payload.agrupacion.categoria) {
          payload.agrupacion.categoria = 'OTRO';
        }
        
        payload.agrupacion.agrupacionOtro = {
          comentariosDestacables: v.comentariosDestacables
        };
      }
    }

    // ======================================================================
    // 3. ENVÍO DE LA PETICIÓN AL SERVICIO
    // ======================================================================
    this.agrupacionService.crearInscripcion(payload).subscribe({
      next: () => {
        alert('¡Inscripción realizada con éxito!');
        this.toggleFormulario(false); // Cierra el modal/formulario en la vista
        formulario.resetForm(); // Limpia los inputs del HTML
        // Recarga el listado de la tabla principal dejando un pequeño margen para la BD
        setTimeout(() => this.cargarDatos(Number(idLogueado)), 200);
      },
      error: (err) => {
        console.error('Error al procesar la inscripción:', err);
        alert('Hubo un error en el servidor al guardar la inscripción.');
      }
    });
  }
}