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

  loading: boolean = true;
  exitoCargaContexto: boolean = false;
  mostrarFormulario: boolean = false;
  mensajeErrorForm: string | null = null;

  mostrarModalExito: boolean = false;
  tituloModalExito: string = '';
  contenidoModalExito: string = '';

  mostrarModalError: boolean = false;
  tituloModalError: string = '';
  contenidoModalError: string = '';

  mostrarModalConfirmar: boolean = false;
  idParticipacionAEliminar: number | null = null;

  dniBusqueda: string = '';
  participanteEncontradoHistorico: any = null;
  
  nuevoParticipante: any = {
    idParticipacion: null,
    idParticipanteBase: null, 
    nombre: '',
    dni: '',
    fechaNacimiento: '',
    rol: 'VOZ'
  };

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private cd: ChangeDetectorRef
  ) { }

  get esHistorico(): boolean {
    if (!this.inscripcionActiva || !this.inscripcionActiva.concurso) {
      return false;
    }
    const concurso = this.inscripcionActiva.concurso;
    const estadoEnum = (concurso.estadoConcurso || concurso.estado || '').toUpperCase().trim();

    if (estadoEnum === 'HISTORICO') return true;
    if (estadoEnum === 'ACTIVO') return false;

    if (concurso.fechaFin) {
      const fechaFinConcurso = new Date(concurso.fechaFin);
      const fechaHoy = new Date();
      if (fechaFinConcurso < fechaHoy) return true;
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
    
    if (!regexDni.test(d)) return false;

    const letras = 'TRWAGMYFPDXBNJZSQVHLCKE';
    const numero = parseInt(d.substring(0, 8), 10);
    const letraAsignada = d.charAt(8);
    const letraCorrecta = letras.charAt(numero % 23);

    return letraAsignada === letraCorrecta;
  }

  verificarDniAutomatico() {
    if (this.dniBusqueda && this.validarFormFormatDni(this.dniBusqueda)) {
      this.buscarParticipantePorDni();
    }
  }

  detectarDniEscritoAManos() {
    if (!this.nuevoParticipante.idParticipacion && this.nuevoParticipante.dni) {
      const dniLimpio = this.nuevoParticipante.dni.trim().toUpperCase();
      if (this.validarFormFormatDni(dniLimpio)) {
        
        const yaExisteLocal = this.listaParticipantes.some(
          p => p.participante?.dni?.toUpperCase() === dniLimpio
        );

        if (yaExisteLocal) {
          this.mensajeErrorForm = 'Este participante ya figura inscrito en este listado.';
          return;
        }

        this.dniBusqueda = dniLimpio;
        this.buscarParticipantePorDni();
      }
    }
  }

  abrirFormulario() {
    if (this.esHistorico) return;
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
      rol: 'VOZ'
    };
    if (this.partForm) {
      this.partForm.resetForm({ rol: 'VOZ' });
    }
  }

  buscarParticipantePorDni() {
    if (this.esHistorico) return;
    this.mensajeErrorForm = null;
    this.participanteEncontradoHistorico = null;

    if (!this.dniBusqueda.trim()) return;

    if (!this.validarFormFormatDni(this.dniBusqueda)) {
      this.mensajeErrorForm = 'El formato del DNI introducido no es válido.';
      return;
    }

    const yaExisteEnEstaInscripcion = this.listaParticipantes.some(
      p => p.participante?.dni?.toUpperCase() === this.dniBusqueda.trim().toUpperCase()
    );
    
    if (yaExisteEnEstaInscripcion) {
      this.mensajeErrorForm = 'Este participante ya figura inscrito en este listado actual.';
      return;
    }

    const adminId = localStorage.getItem('idUsuario') || localStorage.getItem('idAdministrador') || localStorage.getItem('id') || '1';

    this.http.get<any>(`http://localhost:8080/api/participantes/buscar-historico?dni=${this.dniBusqueda.trim().toUpperCase()}&idUsuarioActual=${adminId}`)
      .subscribe({
        next: (res) => {
          if (res) {
            this.participanteEncontradoHistorico = res;
            this.mensajeErrorForm = '💡 El participante ya existe en el sistema. Por seguridad, debe pulsar el botón "Importar Datos" que ha aparecido arriba antes de guardar.';
          }
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error('Error al verificar DNI:', err);
          if (err.status === 403) {
            this.manejarBloqueoSeguridad();
          } else {
            this.mensajeErrorForm = 'Error al consultar el histórico del servidor.';
            this.cd.detectChanges();
          }
        }
      });
  }

  importarParticipanteEncontrado() {
    if (this.participanteEncontradoHistorico) {
      const idBase = this.participanteEncontradoHistorico.id || this.participanteEncontradoHistorico.idParticipante;

      let fechaLimpia = '';
      if (this.participanteEncontradoHistorico.fechaNacimiento) {
        fechaLimpia = this.participanteEncontradoHistorico.fechaNacimiento.split('T')[0];
      }

      this.nuevoParticipante = {
        idParticipacion: null, 
        idParticipanteBase: idBase, 
        nombre: this.participanteEncontradoHistorico.nombre,
        dni: this.participanteEncontradoHistorico.dni || this.dniBusqueda.toUpperCase(), 
        fechaNacimiento: fechaLimpia,
        rol: 'VOZ'
      };
      
      this.participanteEncontradoHistorico = null;
      this.mensajeErrorForm = null; 
      this.cd.detectChanges();
    }
  }

  cargarDatosContexto(idInscripcion: string) {
    const adminId = localStorage.getItem('idUsuario') || localStorage.getItem('idAdministrador') || localStorage.getItem('id') || '1';

    this.http.get<any>(`http://localhost:8080/api/inscripciones/${idInscripcion}?idUsuarioActual=${adminId}`)
      .subscribe({
        next: (res) => {
          this.inscripcionActiva = res;
          this.exitoCargaContexto = true;
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error('Error cargando cabecera', err);
          if (err.status === 403) {
            this.manejarBloqueoSeguridad();
          }
        }
      });
  }

  cargarParticipantes(idInscripcion: string) {
    this.loading = true;
    const adminId = localStorage.getItem('idUsuario') || localStorage.getItem('idAdministrador') || localStorage.getItem('id') || '1';

    this.http.get<any[]>(`http://localhost:8080/api/participantes/inscripcion/${idInscripcion}?idUsuarioActual=${adminId}`)
      .subscribe({
        next: (data) => {
          this.listaParticipantes = data ? data : [];
          this.loading = false;
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error('Error cargando participantes:', err);
          this.loading = false;
          if (err.status === 403) {
            this.manejarBloqueoSeguridad();
          } else {
            this.cd.detectChanges();
          }
        }
      });
  }

  guardarParticipante(form: NgForm) {
    if (this.esHistorico || !this.idInscripcionActual) return;

    if (this.participanteEncontradoHistorico) {
      this.mensajeErrorForm = 'Debe pulsar el botón "Importar Datos" antes de registrar este DNI existente.';
      return;
    }

    this.mensajeErrorForm = null;
    const dniAEnviar = this.nuevoParticipante.dni.trim().toUpperCase();

    if (!this.validarFormFormatDni(dniAEnviar)) {
      this.mensajeErrorForm = 'La estructura del DNI/NIE es incorrecta o la letra no se corresponde.';
      return;
    }

    if (!this.nuevoParticipante.idParticipacion) {
      const duplicadoLocal = this.listaParticipantes.some(
        p => p.participante?.dni?.toUpperCase() === dniAEnviar
      );
      if (duplicadoLocal) {
        this.mensajeErrorForm = 'Este participante ya se encuentra registrado en esta agrupación.';
        return;
      }
    }

    let rolFinalEnum = this.nuevoParticipante.rol ? String(this.nuevoParticipante.rol).toUpperCase().trim() : 'VOZ';
    const adminId = localStorage.getItem('idUsuario') || localStorage.getItem('idAdministrador') || localStorage.getItem('id') || '1';

    const payloadDefinitivo = {
      idParticipacion: this.nuevoParticipante.idParticipacion,
      idInscripcion: Number(this.idInscripcionActual),
      idParticipante: this.nuevoParticipante.idParticipanteBase,
      nombre: this.nuevoParticipante.nombre.trim(),
      dni: dniAEnviar,
      fechaNacimiento: this.nuevoParticipante.fechaNacimiento,
      rol: rolFinalEnum,
      idUsuarioActual: Number(adminId),
      inscripcion: {
        idInscripcion: Number(this.idInscripcionActual)
      },
      participante: {
        id: this.nuevoParticipante.idParticipanteBase,
        idParticipante: this.nuevoParticipante.idParticipanteBase,
        nombre: this.nuevoParticipante.nombre.trim(),
        dni: dniAEnviar,
        fechaNacimiento: this.nuevoParticipante.fechaNacimiento
      }
    };

    this.http.post('http://localhost:8080/api/participantes/guardar', payloadDefinitivo)
      .subscribe({
        next: () => {
          this.tituloModalExito = this.nuevoParticipante.idParticipacion ? '¡Registro Actualizado!' : '¡Participante Guardado!';
          this.contenidoModalExito = this.nuevoParticipante.idParticipacion 
            ? 'Los cambios se han guardado en la ficha existente.' 
            : 'El integrante ha sido registrado y vinculado de forma correcta.';
          
          this.mostrarModalExito = true;
          this.cerrarFormulario();
          this.cargarParticipantes(this.idInscripcionActual!);
        },
        error: (err) => {
          console.error('Error detallado de respuesta backend:', err);
          if (err.status === 403) {
            this.cerrarFormulario();
            this.manejarBloqueoSeguridad();
          } else {
            this.mensajeErrorForm = err.error?.error || err.error?.message || 'Error de procesamiento en el servidor.';
            this.cd.detectChanges();
          }
        }
      });
  }

  editarParticipante(part: any) {
    if (this.esHistorico) return;
    this.mostrarFormulario = true;
    this.mensajeErrorForm = null;

    let rolBackend = part.rol || part.role || 'VOZ';
    rolBackend = rolBackend.trim().toUpperCase().replace(/ /g, '_'); 

    if (rolBackend.includes('AYUDANTE')) {
      rolBackend = 'AYUDANTE_DE_ESCENA';
    } else if (rolBackend.includes('MONTADOR')) {
      rolBackend = 'MONTADOR';
    } else if (rolBackend.includes('MAQUILLA')) {
      rolBackend = 'MAQUILLADORA';
    } else if (rolBackend.includes('OTRO')) {
      rolBackend = 'OTRO';
    }
    
    this.nuevoParticipante = {
      idParticipacion: part.idParticipacion,
      idParticipanteBase: part.participante?.id || part.participante?.idParticipante,
      nombre: part.participante?.nombre || '',
      dni: part.participante?.dni || '',
      fechaNacimiento: part.participante?.fechaNacimiento ? part.participante.fechaNacimiento.split('T')[0] : '',
      rol: rolBackend
    };

    setTimeout(() => {
      if (this.partForm && this.partForm.controls['rol']) {
        this.partForm.controls['rol'].setValue(rolBackend);
      }
      this.cd.detectChanges();
    }, 50);
  }

  solicitarEliminarParticipante(idParticipacion: number) {
    if (this.esHistorico) return; 
    this.idParticipacionAEliminar = idParticipacion;
    this.mostrarModalConfirmar = true;
    this.cd.detectChanges();
  }

  confirmarEliminar() {
    if (this.esHistorico || !this.idParticipacionAEliminar) return;

    const adminId = localStorage.getItem('idUsuario') || localStorage.getItem('idAdministrador') || localStorage.getItem('id') || '1';

    this.http.delete(`http://localhost:8080/api/participantes/eliminar/${this.idParticipacionAEliminar}?idUsuarioActual=${adminId}`)
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
          this.contenidoModalError = err.error?.error || 'Error al borrar el registro del servidor.';
          this.mostrarModalError = true;
          this.cd.detectChanges();
        }
      });
  }

  private manejarBloqueoSeguridad() {
    this.loading = false;
    this.exitoCargaContexto = false;
    this.tituloModalError = 'Acceso Restringido';
    this.contenidoModalError = 'Seguridad LOPD: No dispones de los permisos requeridos para gestionar o visualizar los integrantes de esta agrupación.';
    this.mostrarModalError = true;
    this.cd.detectChanges();
    setTimeout(() => {
      this.irAlPanel();
    }, 3000);
  }
}