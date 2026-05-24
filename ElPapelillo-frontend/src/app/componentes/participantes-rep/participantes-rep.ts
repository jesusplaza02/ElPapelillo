import { Component, OnInit, ChangeDetectorRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';

@Component({
  selector: 'app-gestion-participantes',
  standalone: true,
  imports: [CommonModule, HttpClientModule, FormsModule],
  templateUrl: './participantes-rep.html',
  styleUrl: './participantes-rep.css'
})
export class GestionParticipantesComponent implements OnInit {

  @ViewChild('partForm') partForm!: NgForm;

  listaParticipantes: any[] = [];
  idInscripcionActual: string | null = null;
  inscripcionActiva: any = null;

  // --- VARIABLES DE ESTADO Y DIÁLOGOS ---
  loading: boolean = true;
  mostrarFormulario: boolean = false;
  mensajeErrorForm: string | null = null;

  // VARIABLES PARA LAS VENTANAS MODALES INTEGRADAS
  mostrarModalExito: boolean = false;
  tituloModalExito: string = '';
  contenidoModalExito: string = '';

  mostrarModalError: boolean = false;
  tituloModalError: string = '';
  contenidoModalError: string = '';

  mostrarModalConfirmar: boolean = false;
  idParticipacionAEliminar: number | null = null;

  // --- MODELOS DE BÚSQUEDA E INSERCIÓN ---
  dniBusqueda: string = '';
  participanteEncontradoHistorico: any = null;
  
  nuevoParticipante: any = {
    idParticipacion: null,
    idParticipanteBase: null, 
    nombre: '',
    dni: '',
    fechaNacimiento: '',
    role: 'Voz'
  };

  public readonly ROLES_DISPONIBLES = [
    { grupo: 'Componentes', roles: ['Voz', 'Guitarra', 'Caja', 'Bombo'] },
    { grupo: 'Personal Auxiliar', roles: ['Ayudantes de escena', 'Montadores', 'Maquilladoras', 'Otros'] }
  ];

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private cd: ChangeDetectorRef
  ) { }

  /**
   * Getter seguro para verificar si la inscripción pertenece a un concurso histórico.
   * Filtra por el Enum EstadoConcurso de Spring Boot y maneja salvaguarda temporal.
   */
  get esHistorico(): boolean {
    if (!this.inscripcionActiva || !this.inscripcionActiva.concurso) {
      return false;
    }

    const concurso = this.inscripcionActiva.concurso;
    const estadoEnum = (concurso.estadoConcurso || concurso.estado || '').toUpperCase().trim();

    console.log('[DEBUG-ENUM] Valor del estado del concurso detectado:', estadoEnum);

    if (estadoEnum === 'HISTORICO') {
      return true;
    }

    if (estadoEnum === 'ACTIVO') {
      return false;
    }

    // Salvaguarda por fecha (por si el registro en BBDD no tuviera el Enum asignado)
    if (concurso.fechaFin) {
      const fechaFinConcurso = new Date(concurso.fechaFin);
      const fechaHoy = new Date();
      if (fechaFinConcurso < fechaHoy) {
        return true;
      }
    }

    return false;
  }

  ngOnInit(): void {
    this.idInscripcionActual = this.route.snapshot.paramMap.get('id');
    if (this.idInscripcionActual) {
      this.cargarDatosContexto(this.idInscripcionActual);
      this.cargarParticipantes(this.idInscripcionActual);
    } else {
      this.irAlPanel();
    }
  }

  irAlPanel() {
    this.router.navigate(['/panel-representante']);
  }

  validarFormFormatDni(dni: string): boolean {
    const d = dni.trim().toUpperCase();
    const regexDni = /^[0-9]{8}[TRWAGMYFPDXBNJZSQVHLCKE]$/;
    
    if (!regexDni.test(d)) {
      return false;
    }

    const letras = 'TRWAGMYFPDXBNJZSQVHLCKE';
    const numero = parseInt(d.substring(0, 8), 10);
    const letraAsignada = d.charAt(8);
    const letraCorrecta = letras.charAt(numero % 23);

    return letraAsignada === letraCorrecta;
  }

  abrirFormulario() {
    if (this.esHistorico) return; // Bloqueo estricto
    this.mostrarFormulario = true;
    this.limpiarFormulario();
    this.cd.detectChanges();
  }

  cerrarFormulario() {
    this.mostrarFormulario = false;
    this.limpiarFormulario();
    this.cd.detectChanges();
  }

  limpiarFormulario() {
    this.dniBusqueda = '';
    this.participanteEncontradoHistorico = null;
    this.mensajeErrorForm = null;
    this.nuevoParticipante = {
      idParticipacion: null,
      idParticipanteBase: null,
      nombre: '',
      dni: '',
      fechaNacimiento: '',
      rol: 'Voz'
    };
    if (this.partForm) {
      this.partForm.resetForm({ rol: 'Voz' });
    }
  }

  buscarParticipantePorDni() {
    if (this.esHistorico) return;

    this.mensajeErrorForm = null;
    this.participanteEncontradoHistorico = null;

    if (!this.dniBusqueda.trim()) {
      this.mensajeErrorForm = '⚠️ Por favor, introduce un DNI para realizar la comprobación.';
      return;
    }

    if (!this.validarFormFormatDni(this.dniBusqueda)) {
      this.mensajeErrorForm = '⚠️ El formato del DNI introducido no es válido o la letra de control es incorrecta.';
      return;
    }

    const yaExisteEnEstaInscripcion = this.listaParticipantes.some(
      p => p.participante?.dni?.toUpperCase() === this.dniBusqueda.trim().toUpperCase()
    );
    
    if (yaExisteEnEstaInscripcion) {
      this.mensajeErrorForm = '⚠️ Operación denegada: Este participante ya figura inscrito en este mismo listado.';
      return;
    }

    this.http.get<any>(`http://localhost:8080/api/participantes/buscar-historico?dni=${this.dniBusqueda.trim().toUpperCase()}`)
      .subscribe({
        next: (res) => {
          if (res) {
            this.participanteEncontradoHistorico = res;
          } else {
            this.tituloModalExito = 'Sin registros previos';
            this.contenidoModalExito = 'No se registran participaciones previas con este DNI en el sistema. Complete la ficha manualmente.';
            this.mostrarModalExito = true;
            this.nuevoParticipante.dni = this.dniBusqueda.trim().toUpperCase();
          }
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error('Error al verificar DNI:', err);
          this.mensajeErrorForm = '⚠️ Error al consultar el histórico del servidor.';
          this.cd.detectChanges();
        }
      });
  }

  importarParticipanteEncontrado() {
    if (this.participanteEncontradoHistorico) {
      const idBase = this.participanteEncontradoHistorico.id || this.participanteEncontradoHistorico.idParticipante;

      this.nuevoParticipante = {
        idParticipacion: null,
        idParticipanteBase: idBase, 
        nombre: this.participanteEncontradoHistorico.nombre,
        dni: this.participanteEncontradoHistorico.dni || this.dniBusqueda.toUpperCase(), 
        fechaNacimiento: this.participanteEncontradoHistorico.fechaNacimiento ? this.participanteEncontradoHistorico.fechaNacimiento.split('T')[0] : '',
        rol: this.nuevoParticipante.rol || 'Voz' 
      };
      
      this.participanteEncontradoHistorico = null;
      this.mensajeErrorForm = null; 
      this.cd.detectChanges();
    }
  }

  cargarDatosContexto(idInscripcion: string) {
    this.http.get<any>(`http://localhost:8080/api/inscripciones/${idInscripcion}`)
      .subscribe({
        next: (res) => {
          this.inscripcionActiva = res;
          this.cd.detectChanges();
        },
        error: (err) => console.error('Error cargando cabecera', err)
      });
  }

  cargarParticipantes(idInscripcion: string) {
    this.loading = true;
    this.http.get<any[]>(`http://localhost:8080/api/participantes/inscripcion/${idInscripcion}`)
      .subscribe({
        next: (data) => {
          this.listaParticipantes = data ? data : [];
          this.loading = false;
          this.cd.markForCheck();
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error('Error cargando participantes:', err);
          this.loading = false;
          this.cd.detectChanges();
        }
      });
  }

  guardarParticipante(form: NgForm) {
    if (this.esHistorico || !form.valid || !this.idInscripcionActual) return;
    this.mensajeErrorForm = null;

    const dniAEnviar = this.nuevoParticipante.dni.trim().toUpperCase();

    if (!this.validarFormFormatDni(dniAEnviar)) {
      this.mensajeErrorForm = '⚠️ La estructura del DNI/NIE es incorrecta o la letra no se corresponde.';
      this.cd.detectChanges();
      return;
    }

    if (!this.nuevoParticipante.idParticipacion) {
      const duplicado = this.listaParticipantes.some(
        p => p.participante?.dni?.toUpperCase() === dniAEnviar
      );
      if (duplicado) {
        this.mensajeErrorForm = '⚠️ Operación cancelada: Este participante ya se encuentra registrado en el listado de esta agrupación.';
        this.cd.detectChanges();
        return;
      }
    }
    
    const payload = {
      idParticipacion: this.nuevoParticipante.idParticipacion,
      idParticipante: this.nuevoParticipante.idParticipanteBase, 
      nombre: this.nuevoParticipante.nombre,
      dni: dniAEnviar,
      fechaNacimiento: this.nuevoParticipante.fechaNacimiento,
      rol: this.nuevoParticipante.rol,
      inscripcion: { idInscripcion: Number(this.idInscripcionActual) }
    };

    this.http.post('http://localhost:8080/api/participantes/guardar', payload)
      .subscribe({
        next: () => {
          this.tituloModalExito = '¡Participante Guardado!';
          this.contenidoModalExito = 'El integrante ha sido registrado de forma correcta en los expedientes de la agrupación.';
          this.mostrarModalExito = true;
          this.cerrarFormulario();
          this.cargarParticipantes(this.idInscripcionActual!);
        },
        error: (err) => {
          if (err.status === 400 || err.error?.error?.includes('ConstraintViolation')) {
            this.mensajeErrorForm = '⚠️ Conflicto de base de datos: El DNI ya pertenece a un usuario. Asegúrate de pulsar "Importar Datos" arriba tras verificar.';
          } else {
            this.mensajeErrorForm = err.error?.error || 'Error de procesamiento en el servidor.';
          }
          this.cd.detectChanges();
        }
      });
  }

  editarParticipante(part: any) {
    if (this.esHistorico) return; // Cortafuegos preventivo

    this.mostrarFormulario = true;
    this.mensajeErrorForm = null;
    
    this.nuevoParticipante = {
      idParticipacion: part.idParticipacion,
      idParticipanteBase: part.participante?.id || part.participante?.idParticipante,
      nombre: part.participante?.nombre || '',
      dni: part.participante?.dni || '',
      fechaNacimiento: part.participante?.fechaNacimiento ? part.participante.fechaNacimiento.split('T')[0] : '',
      rol: part.rol
    };
    
    this.cd.markForCheck();
    this.cd.detectChanges();
  }

  solicitarEliminarParticipante(idParticipacion: number) {
    if (this.esHistorico) return; 
    this.idParticipacionAEliminar = idParticipacion;
    this.mostrarModalConfirmar = true;
    this.cd.detectChanges();
  }

  enmascararDniVisual(dni: string | null | undefined): string {
    if (!dni) return '';
    const dniLimpio = dni.trim();
    if (dniLimpio.includes('*')) return dniLimpio;
    if (dniLimpio.length < 5) return dniLimpio;
    return dniLimpio.substring(0, 4) + '****' + dniLimpio.substring(dniLimpio.length - 1);
  }

  confirmarEliminar() {
    if (this.esHistorico || !this.idParticipacionAEliminar) return;

    this.http.delete(`http://localhost:8080/api/participantes/eliminar/${this.idParticipacionAEliminar}`)
      .subscribe({
        next: () => {
          this.mostrarModalConfirmar = false;
          this.idParticipacionAEliminar = null;
          
          this.tituloModalExito = 'Registro Eliminado';
          this.contenidoModalExito = 'El participante se ha desvinculado con éxito de la inscripción.';
          this.mostrarModalExito = true;

          this.cargarParticipantes(this.idInscripcionActual!);
        },
        error: (err) => {
          this.mostrarModalConfirmar = false;
          this.idParticipacionAEliminar = null;
          
          this.tituloModalError = 'No se pudo eliminar';
          this.contenidoModalError = err.error?.error || 'Se ha detectado un problema de integridad al intentar borrar este registro del servidor.';
          this.mostrarModalError = true;
          this.cd.detectChanges();
        }
      });
  }
}