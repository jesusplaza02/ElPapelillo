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
  styleUrls: ['./participantes-rep.css']
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

  // --- MODELOS DE BÚSQUEDA E INSERCIÓN ---
  dniBusqueda: string = '';
  participanteEncontradoHistorico: any = null;
  
  nuevoParticipante: any = {
    idParticipacion: null,
    idParticipanteBase: null, // 🔑 ID de la tabla maestra 'participante'
    nombre: '',
    dni: '',
    fechaNacimiento: '',
    rol: 'Voz'
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

  // --- 🧮 VALIDACIÓN DE FORMATO DE DNI ---
  validarFormatoDni(dni: string): boolean {
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

  // --- FLUJO DE CONTROL DE FORMULARIO ---
  abrirFormulario() {
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

  // --- 🔍 BÚSQUEDA POR DNI CON VALIDACIÓN DE AGREGACIÓN ACTUAL ---
  buscarParticipantePorDni() {
    this.mensajeErrorForm = null;
    this.participanteEncontradoHistorico = null;

    if (!this.dniBusqueda.trim()) {
      this.mensajeErrorForm = '⚠️ Por favor, introduce un DNI para realizar la comprobación.';
      return;
    }

    if (!this.validarFormatoDni(this.dniBusqueda)) {
      this.mensajeErrorForm = '⚠️ El formato del DNI introducido no es válido o la letra de control es incorrecta.';
      return;
    }

    // 🔒 REGLA DE NEGOCIO 1: Impedir duplicar el mismo DNI dentro de la inscripción de la misma agrupación
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
            alert('No se registran participaciones previas con este DNI en el sistema. Complete la ficha manualmente.');
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

  // --- 📥 REPARACIÓN CLAVE DE IMPORTACIÓN ---
  importarParticipanteEncontrado() {
    if (this.participanteEncontradoHistorico) {
      // Forzamos la obtención correcta de la ID primaria ('id' o 'idParticipante') dependiendo de tu modelo Java
      const idBase = this.participanteEncontradoHistorico.id || this.participanteEncontradoHistorico.idParticipante;

      this.nuevoParticipante = {
        idParticipacion: null, // Sigue siendo un alta de fila intermedia nueva...
        idParticipanteBase: idBase, // 🔑 ¡ESTO ARREGLA EL PROBLEMA! Le inyectamos el ID maestro real mapeado para que Java no intente crear un clon duplicado
        nombre: this.participanteEncontradoHistorico.nombre,
        dni: this.participanteEncontradoHistorico.dni || this.dniBusqueda.toUpperCase(), 
        fechaNacimiento: this.participanteEncontradoHistorico.fechaNacimiento ? this.participanteEncontradoHistorico.fechaNacimiento.split('T')[0] : '',
        rol: this.nuevoParticipante.rol || 'Voz' 
      };
      
      this.participanteEncontradoHistorico = null;
      this.mensajeErrorForm = null; // Limpiamos alertas previas de advertencia
      this.cd.detectChanges();
    }
  }

  // --- ASÍNCRONOS HTTP ---
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

  // --- 💾 GUARDAR / ACTUALIZAR CON CONTROL TOTAL DE UNICIDAD ---
  guardarParticipante(form: NgForm) {
    if (!form.valid || !this.idInscripcionActual) return;
    this.mensajeErrorForm = null;

    const dniAEnviar = this.nuevoParticipante.dni.trim().toUpperCase();

    if (!this.validarFormatoDni(dniAEnviar)) {
      this.mensajeErrorForm = '⚠️ La estructura del DNI/NIE es incorrecta o la letra no se corresponde.';
      this.cd.detectChanges();
      return;
    }

    // 🔒 REGLA DE NEGOCIO 2: Validación doble en el guardado (por si el usuario cambia el input a mano tras buscar)
    // Solo aplica si estamos creando una vinculación nueva (idParticipacion nulo)
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
      idParticipante: this.nuevoParticipante.idParticipanteBase, // Enviamos el ID maestro recuperado en la importación
      nombre: this.nuevoParticipante.nombre,
      dni: dniAEnviar,
      fechaNacimiento: this.nuevoParticipante.fechaNacimiento,
      rol: this.nuevoParticipante.rol,
      inscripcion: { idInscripcion: Number(this.idInscripcionActual) }
    };

    this.http.post('http://localhost:8080/api/participantes/guardar', payload)
      .subscribe({
        next: () => {
          alert('¡Participante guardado correctamente!');
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

  eliminarParticipante(idParticipacion: number) {
    if (!confirm('¿Estás seguro de que deseas eliminar este participante de la agrupación?')) return;

    this.http.delete(`http://localhost:8080/api/participantes/eliminar/${idParticipacion}`)
      .subscribe({
        next: () => {
          alert('Registro eliminado de la inscripción.');
          this.cargarParticipantes(this.idInscripcionActual!);
        },
        error: (err) => {
          alert('No se pudo eliminar la participación de la base de datos.');
          this.cd.detectChanges();
        }
      });
  }

  enmascararDniVisual(dni: string): string {
    if (!dni) return '';
    if (dni.includes('*')) return dni;
    if (dni.length < 5) return dni;
    return dni.substring(0, 4) + '****' + dni.substring(dni.length - 1);
  }
}