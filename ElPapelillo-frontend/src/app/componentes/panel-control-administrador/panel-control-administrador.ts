import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { catchError, forkJoin, of } from 'rxjs';

import { Usuario } from './panel-control-administrador-usuario.model'; 
import { PanelControlAdministradorUsuarioService } from './panel-control-administrador-usuario.service';
import { PanelControlAdministradorAuditoriaService } from './panel-control-administrador-auditoria.service';
import { PanelControlAdministradorOrganizacionService } from './panel-control-administrador-organizacion.service';
import { PanelControlAdministradorConcursoService } from './panel-control-administrador-concurso.service';

@Component({
  selector: 'app-panel-control-administrador',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './panel-control-administrador.html',
  styleUrl: './panel-control-administrador.css'
})
export class PanelControlAdministradorComponent implements OnInit {
  isMenuOpen = false;
  borradoExitoso: boolean = false;
  
  // USUARIOS
  usuarios: Usuario[] = [];
  usuariosFiltrados: Usuario[] = [];
  usuariosPaginados: Usuario[] = []; 
  terminoBusqueda: string = '';
  paginaActual: number = 1;
  totalPaginas: number = 1;

  // AUDITORÍA
  logs: any[] = [];
  logsFiltrados: any[] = [];
  logsPaginados: any[] = []; // Esta es la que debes usar en el *ngFor de la auditoría
  paginaActualLogs: number = 1;
  totalPaginasLogs: number = 1;
  filtroCategoria: string = 'Todos';
  filtroFecha: string = '';

  itemsPorPagina: number = 5;
  rolSesionActual: string | null = '';
  
  // FORMULARIO USUARIOS
  mostrandoFormulario = false;
  modoFormulario: 'crear' | 'editar' | 'ver' = 'crear';
  nuevoUsuario: any = {
    nombre: '', email: '', dni: '', direccion: '', 
    telefono: '', rol: 'REPRESENTANTE', contacto_emergencia: '', cargo: '', activo: true 
  };

  // FORMULARIO CONCURSOS
  mostrandoFormularioConcurso = false;
  modoFormularioConcurso: 'crear' | 'editar' | 'ver' = 'ver';
  concursoSeleccionado: any = {};

  // MODAL BORRADO
  mostrarModalBorrado = false;
  usuarioABorrar: any = null;
  mensajeErrorBorrado: string | null = null;

  // ORGANIZACIONES
  organizaciones: any[] = [];
  organizacionesFiltradas: any[] = [];
  terminoBusquedaOrg: string = '';
  mostrandoFormOrg: boolean = false;
  modoFormOrg: 'crear' | 'editar' = 'crear';
  mostrarModalBorradoOrg: boolean = false; // Fundamental para que el modal de borrar aparezca/desaparezca
  orgABorrar: any = null;
  nuevaOrg: any = { 
    idOrganizacion: null, 
    nombre: '', 
    email: '',
    telefono: '', 
    ubicacion: '',
    activo: true 
  };

  // Variables para el modal de borrado de concursos
  mostrarModalBorradoConcurso = false;
  esErrorModalBorradoConcurso = false;
  mensajeModalBorradoConcurso = '';
  tituloModalBorradoConcurso = '';
  idConcursoABorrar: number | null = null;

  // 2. Añade las variables de estado para concursos
  concursos: any[] = [];
  concursosFiltrados: any[] = [];
  concursosPaginados: any[] = [];
  terminoBusquedaConcurso: string = '';
  filtroEstadoConcurso: string = '';
  filtroOrgConcurso: string = '';

  paginaActualConcursos: number = 1;
  itemsPorPaginaConcursos: number = 5;
  totalPaginasConcursos: number = 1;
  

  // Control de Toasts
  mostrarToast: boolean = false;
  mensajeToast: string = '';
  tipoToast: 'success' | 'error' = 'success';
    

  constructor(
    private usuarioService: PanelControlAdministradorUsuarioService,
    private auditoriaService: PanelControlAdministradorAuditoriaService,
    private organizacionService: PanelControlAdministradorOrganizacionService,
    private concursoService: PanelControlAdministradorConcursoService
  ) {}

  ngOnInit(): void {
    this.rolSesionActual = localStorage.getItem('rol'); 
    this.cargarDatosSincronizados();
    // Si soy SYSADMIN, cargo también las organizaciones
    if (this.rolSesionActual?.toUpperCase() === 'SYSADMIN') {
      this.cargarOrganizaciones();
    }
  }

  // Objeto para el formulario (debe coincidir con los campos del HTML)

// --- MÉTODOS DE ORGANIZACIONES ---
// Método para disparar el toast y que se oculte solo
lanzarToast(mensaje: string, tipo: 'success' | 'error' = 'success'): void {
  this.mensajeToast = mensaje;
  this.tipoToast = tipo;
  this.mostrarToast = true;
  
  // Se oculta automáticamente tras 3 segundos
  setTimeout(() => {
    this.mostrarToast = false;
  }, 3000);
}

cargarOrganizaciones(): void {
  this.organizacionService.getOrganizaciones().subscribe({
    next: (res) => {
      // Filtramos en el cliente para mostrar solo las activas
      this.organizaciones = res.filter((o: any) => o.activo !== false && o.activo !== 0);
      this.organizacionesFiltradas = [...this.organizaciones];
    },
    error: (err) => console.error("Error al cargar organizaciones", err)
  });
}

buscarOrgs(): void {
  const termino = this.terminoBusquedaOrg.toLowerCase().trim();
  if (!termino) {
    this.organizacionesFiltradas = [...this.organizaciones];
  } else {
    this.organizacionesFiltradas = this.organizaciones.filter(org => 
      org.nombre?.toLowerCase().includes(termino) || 
      org.cif?.toLowerCase().includes(termino)
    );
  }
}

abrirModalOrg(): void {
  this.modoFormOrg = 'crear';
  this.nuevaOrg = { idOrganizacion: null, nombre: '', email: '', telefono: '', ubicacion: '', activo: true };
  this.mostrandoFormOrg = true;
}

editarOrg(org: any): void {
  this.modoFormOrg = 'editar';
  // Mapeamos los datos asegurando que emailContacto y direccion existan para el form
  this.nuevaOrg = { 
    ...org,
    email: org.email, 
    ubicacion: org.ubicacion,
    telefono: org.telefono 
  }; 
  this.mostrandoFormOrg = true;
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

cancelarFormOrg(): void {
  this.mostrandoFormOrg = false;
}

cerrarTodoOrg(): void {
  console.log("Forzando cierre de modales..."); // Mira si esto sale en la consola (F12)
  
  this.mostrandoFormOrg = false;
  this.mostrarModalBorradoOrg = false;
  this.orgABorrar = null;

  // Reseteamos el objeto de la organización
  this.nuevaOrg = { 
    idOrganizacion: null, 
    nombre: '', 
    emailContacto: '', 
    telefono: '', 
    direccion: '', 
    activo: true 
  };

  this.cargarOrganizaciones(); // Refrescar la tabla
}

guardarOrganizacion(): void {
  const id = this.nuevaOrg.idOrganizacion;
  const peticion = this.modoFormOrg === 'crear' 
    ? this.organizacionService.crearOrganizacion(this.nuevaOrg)
    : this.organizacionService.actualizarOrganizacion(+id, this.nuevaOrg);

  peticion.subscribe({
    next: () => {
      this.lanzarToast('¡Conseguido!', 'success');
      this.cerrarTodoOrg();
    },
    error: (err) => {
      // Si el servidor responde 200 pero Angular cree que es error (por JSON vacío)
      if (err.status === 200 || err.status === 201) {
        this.lanzarToast('¡Conseguido!', 'success');
        this.cerrarTodoOrg();
      } else {
        this.lanzarToast('Vaya, algo ha fallado', 'error');
        console.error(err);
      }
    }
  });
}
// Creamos un método aparte para limpiar todo y no repetir código
cerrarYRefrescar(): void {
  this.mostrandoFormOrg = false;
  this.cargarOrganizaciones();
  // Limpiamos el objeto para el siguiente uso
  this.nuevaOrg = { idOrganizacion: null, nombre: '', emailContacto: '', telefono: '', direccion: '', activo: true };
}

// --- BORRADO LÓGICO ---

eliminarOrg(org: any): void {
  this.orgABorrar = org;
  this.mostrarModalBorradoOrg = true;
}

cerrarModalBorradoOrg(): void {
  this.mostrarModalBorradoOrg = false;
  this.orgABorrar = null;
}

confirmarBorradoOrg(): void {
  if (this.orgABorrar) {
    const datosDesactivar = { ...this.orgABorrar, activo: false };
    this.organizacionService.actualizarOrganizacion(+this.orgABorrar.idOrganizacion, datosDesactivar).subscribe({
      next: () => {
        this.lanzarToast('Organización eliminada correctamente', 'success');
        this.mostrarModalBorradoOrg = false;
        this.cargarOrganizaciones();
      },
      error: () => this.lanzarToast('No se pudo eliminar', 'error')
    });
  }
}
  
  cargarDatosSincronizados(): void {
  const miRol = (this.rolSesionActual || '').toUpperCase();
  const miIdOrg = localStorage.getItem('id_organizacion');
  const idLogueado = Number(localStorage.getItem('idUsuario'));

  forkJoin({
    usuariosRes: this.usuarioService.getUsuarios(),
    auditoriaRes: this.auditoriaService.getLogs(),
    concursosRes: this.concursoService.getMisConcursos(idLogueado).pipe(catchError(() => of([])))
  }).subscribe({
    next: ({ usuariosRes, auditoriaRes, concursosRes }) => {
      // 1. Usuarios
      const activos = usuariosRes.filter((u: any) => u.activo !== 0 && u.activo !== false);
      this.usuarios = miRol === 'SYSADMIN' 
        ? activos 
        : activos.filter((u: any) => {
            const rolFila = u.rol?.toUpperCase();
            return (rolFila === 'SYSADMIN' || rolFila === 'REPRESENTANTE' || Number(u.id_organizacion) === Number(miIdOrg));
          });
      this.usuariosFiltrados = [...this.usuarios];

      // 2. Auditoría (RESTAURANDO CAMPOS ORIGINALES)
      this.logs = auditoriaRes.map((log: any) => {
        const fechaObj = new Date(log.fecha);
        return {
          ...log,
          icon: this.mapearIconoLog(log.accion), // Para el icono de material
          tipo: this.mapearTipoLog(log.accion),  // Para el color (success, error, info)
          msg: log.accion,                       // El texto de la acción
          cat: this.mapearCategoria(log.accion), // La categoría
          fecha: fechaObj.toLocaleDateString(),  // Fecha bonita
          time: fechaObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }), // Hora bonita
          fechaIso: log.fecha ? log.fecha.split('T')[0] : ''
        };
      });

      // Filtrar logs según organización si no es SYSADMIN
      if (miRol !== 'SYSADMIN') {
        this.logs = this.logs.filter(log => this.usuarios.some(u => (u as any).idUsuario == log.idUsuario));
      }
      this.logsFiltrados = [...this.logs];

      // 3. Procesar Concursos 
      this.concursos = concursosRes;
      this.concursosFiltrados = [...this.concursos];

      // 3. Paginación
      this.actualizarPaginacion();
    },
    error: (err) => console.error("Error crítico en la carga", err)
  });
}

  actualizarPaginacion(): void {
    // Paginación Usuarios
    this.totalPaginas = Math.ceil(this.usuariosFiltrados.length / this.itemsPorPagina) || 1;
    if (this.paginaActual > this.totalPaginas) this.paginaActual = 1;
    const inicioU = (this.paginaActual - 1) * this.itemsPorPagina;
    this.usuariosPaginados = this.usuariosFiltrados.slice(inicioU, inicioU + this.itemsPorPagina);

    // Paginación Auditoría
    this.totalPaginasLogs = Math.ceil(this.logsFiltrados.length / this.itemsPorPagina) || 1;
    if (this.paginaActualLogs > this.totalPaginasLogs) this.paginaActualLogs = 1;
    const inicioL = (this.paginaActualLogs - 1) * this.itemsPorPagina;
    this.logsPaginados = this.logsFiltrados.slice(inicioL, inicioL + this.itemsPorPagina);
  
    // Paginación Concursos
    this.totalPaginasConcursos = Math.ceil(this.concursosFiltrados.length / this.itemsPorPagina) || 1;
    if (this.paginaActualConcursos > this.totalPaginasConcursos) this.paginaActualConcursos = 1;
    const inicioC = (this.paginaActualConcursos - 1) * this.itemsPorPagina;
    this.concursosPaginados = this.concursosFiltrados.slice(inicioC, inicioC + this.itemsPorPagina);
    }

  cambiarPagina(p: number): void {
    if (p >= 1 && p <= this.totalPaginas) {
      this.paginaActual = p;
      this.actualizarPaginacion();
    }
  }

  cambiarPaginaLogs(p: number): void {
    if (p >= 1 && p <= this.totalPaginasLogs) {
      this.paginaActualLogs = p;
      this.actualizarPaginacion();
    }
  }

  buscarUsuarios(): void {
    const termino = this.terminoBusqueda.toLowerCase().trim();
    this.usuariosFiltrados = !termino ? [...this.usuarios] : this.usuarios.filter(u => {
      const user = u as any; 
      return (user.nombre?.toLowerCase().includes(termino)) ||
             (user.email?.toLowerCase().includes(termino)) ||
             (user.dni?.toLowerCase().includes(termino)) ||
             (user.DNI?.toLowerCase().includes(termino));
    });
    this.paginaActual = 1;
    this.actualizarPaginacion();
  }

  filtrarLogs(): void {
    this.logsFiltrados = this.logs.filter(log => {
      const cumpleCat = this.filtroCategoria === 'Todos' || log.cat === this.filtroCategoria;
      const cumpleFecha = !this.filtroFecha || log.fechaIso === this.filtroFecha;
      return cumpleCat && cumpleFecha;
    });
    this.paginaActualLogs = 1;
    this.actualizarPaginacion();
  }

  limpiarFiltros(): void {
    this.filtroCategoria = 'Todos'; 
    this.filtroFecha = ''; 
    this.filtrarLogs();
  }

  // --- MÉTODOS DE APOYO (ESTÉTICA) ---
  private mapearTipoLog(a: string): string { 
    const accion = a?.toUpperCase() || '';
    return accion.includes('BORRADO') ? 'error' : accion.includes('CREAR') ? 'success' : 'info'; 
  }
  private mapearIconoLog(a: string): string { return a?.toUpperCase().includes('USUARIO') ? 'person' : 'settings'; }
  private mapearCategoria(a: string): string { 
    const accion = a?.toUpperCase() || '';
    if (accion.includes('USUARIO')) return 'Usuarios';
    if (accion.includes('DOCUMENTO')) return 'Documentación';
    if (accion.includes('CONCURSO')) return 'Concursos';
    return 'Sistema'; 
  }

  getNombreResponsable(id: any): string {
    const u = this.usuarios.find(user => (user as any).idUsuario == id);
    return u ? u.nombre : 'Sistema';
  }

  // --- PERMISOS Y GESTIÓN ---
  get rolesDisponibles(): string[] {
    const miRol = this.rolSesionActual?.toUpperCase();
    return miRol === 'SUPERADMIN' ? ['REPRESENTANTE', 'ADMINISTRADOR'] : ['REPRESENTANTE'];
  }

  puedeGestionar(u: Usuario): boolean {
    const miRol = this.rolSesionActual?.toUpperCase();
    const rolFila = u.rol?.toUpperCase();
    const miIdOrg = localStorage.getItem('id_organizacion');
    const suIdOrg = (u as any).id_organizacion;
    if (miRol === 'SYSADMIN') return true;
    if (miRol === 'SUPERADMIN') {
      return (rolFila === 'ADMINISTRADOR' && miIdOrg == suIdOrg);
    }
    return false;
  }

  // --- CRUD (Se mantienen tus funciones originales) ---
  abrirModalRegistro(): void { this.modoFormulario = 'crear'; this.resetForm(); this.mostrandoFormulario = true; }
  verDetalles(u: Usuario): void { this.modoFormulario = 'ver'; this.prepararUsuarioParaFormulario(u); this.mostrandoFormulario = true; window.scrollTo({ top: 0, behavior: 'smooth' }); }
  editar(u: Usuario): void { this.modoFormulario = 'editar'; this.prepararUsuarioParaFormulario(u); this.mostrandoFormulario = true; window.scrollTo({ top: 0, behavior: 'smooth' }); }
  private prepararUsuarioParaFormulario(u: Usuario): void { this.nuevoUsuario = { ...u }; const rol = (u as any).rol?.toUpperCase() || ''; this.nuevoUsuario.rol = rol; }
  
  guardarFormulario(): void {
  const miRol = this.rolSesionActual?.toUpperCase();
  const miIdOrg = localStorage.getItem('id_organizacion');
  const idEjecutor = Number(localStorage.getItem('idUsuario'));

  // 1. Creamos el objeto base para enviar
  // IMPORTANTE: Asegúrate de que idUsuario esté presente
  const usuarioParaEnviar: any = { ...this.nuevoUsuario };

  // 2. Inyectamos el discriminador 'type' para Jackson
  // Revisa en tu Java si es 'admin'/'rep' o 'administrador'/'representante'
  const rolLimpio = usuarioParaEnviar.rol?.toUpperCase();
  
  if (rolLimpio === 'REPRESENTANTE') {
    usuarioParaEnviar.type = 'representante';
  } else if (rolLimpio === 'ADMINISTRADOR' || rolLimpio === 'SUPERADMIN') {
    usuarioParaEnviar.type = 'administrador';
  } else {
    // Para el SYSADMIN, no enviamos 'type' de administrador, 
    // para que Jackson lo trate como un Usuario base o el tipo que corresponda.
    delete usuarioParaEnviar.type; 
  }

  if (this.modoFormulario === 'crear') {
    // Lógica de organización para creación
    if (miRol === 'SUPERADMIN' && rolLimpio === 'ADMINISTRADOR') {
      usuarioParaEnviar.id_organizacion = miIdOrg;
    }

    this.usuarioService.crearUsuario(usuarioParaEnviar).subscribe({
      next: () => this.finalizarGuardado('Usuario creado'),
      error: (err) => this.manejarError(err)
    });

  } else if (this.modoFormulario === 'editar') {
    // Verificamos que tenemos el ID, si no, el PUT a /api/usuarios/{id} fallará
    const idAEditar = usuarioParaEnviar.idUsuario || usuarioParaEnviar.id;

    if (!idAEditar) {
      console.error("No se encuentra el ID del usuario para editar:", usuarioParaEnviar);
      alert("Error: No se pudo identificar al usuario para actualizar.");
      return;
    }

    this.usuarioService.actualizarUsuarioConEjecutor(idAEditar, usuarioParaEnviar, idEjecutor).subscribe({
      next: () => this.finalizarGuardado('Usuario actualizado'),
      error: (err) => this.manejarError(err)
    });
  }
}

  guardarConcurso(): void {
      const c = this.concursoSeleccionado;

      // Si es ADMIN normal, nos aseguramos de que lleve SU organización
      if (this.rolSesionActual !== 'SYSADMIN') {
        const idOrgPropia = localStorage.getItem('idOrganizacion');
        if (idOrgPropia) c.id_organizacion = parseInt(idOrgPropia);
      }

      // Validación final antes de enviar
      if (!c.id_organizacion) {
        alert('Debes seleccionar o tener una organización asignada.');
        return;
      }

      // 1. Validaciones de fechas (la lógica que pediste: Ins < Concurso)
      const fIniCon = new Date(c.fechaInicio);
      const fIniIns = new Date(c.fechaInicioInscripcion);
      const fFinIns = new Date(c.fechaFinInscripcion);

      if (fFinIns > fIniCon) {
        alert('Error: La inscripción debe terminar antes de que empiece el concurso.');
        return;
      }

      // 2. IMPORTANTE: Evitar el Error 500 (Campos obligatorios)
      // Si eres ADMIN, te aseguras de enviar tu ID de org
      if (this.rolSesionActual !== 'SYSADMIN') {
        const idOrg = localStorage.getItem('idOrganizacion');
        if (idOrg) c.id_organizacion = parseInt(idOrg);
      }
      // Si eres SYSADMIN, el objeto 'c' ya debería traer su 'id_organizacion' 
      // desde que lo cargaste en la tabla. Si no, asegúrate de que no se borre.

      const servicioCall = this.modoFormularioConcurso === 'crear' 
        ? this.concursoService.crearConcurso(c)
        : this.concursoService.actualizarConcurso(c.idConcurso, c);

      servicioCall.subscribe({
        next: (res) => {
          // ... lógica de actualización de tabla que ya tienes ...
          this.cargarDatosSincronizados();
          this.finalizarGuardado('Guardado correctamente');
        },
        error: (err) => {
          console.error('ERROR 500 DETECTADO:', err);
          alert('Error 500: Revisa que todos los campos (incluida la organización) estén rellenos en la base de datos.');
        }
      });
    }


// Método auxiliar para limpiar la vista tras guardar
private finalizarGuardado(mensaje: string): void {
  alert(mensaje);
  this.cargarDatosSincronizados();
  this.filtrarConcursos(); // Refresca la tabla y paginación
  this.cerrarFormularioConcurso(); // Cierra el formulario
}

  private manejarError(err: any): void {
    console.error("Error completo del backend:", err);
    // Si el backend envía un mensaje de error personalizado, lo mostramos
    const msg = err.error?.message || err.error || 'Error en la operación';
    alert("Error: " + msg);
  }

    resetForm(): void { this.nuevoUsuario = { nombre: '', email: '', dni: '', direccion: '', telefono: '', rol: 'REPRESENTANTE', contacto_emergencia: '', cargo: '', activo: true }; }

    eliminar(u: Usuario): void { this.usuarioABorrar = { ...u }; this.mensajeErrorBorrado = null; this.mostrarModalBorrado = true; }
    confirmarBorrado(): void {
      if (!this.usuarioABorrar) return;
      const miId = Number(localStorage.getItem('idUsuario'));
      const idABorrar = this.usuarioABorrar.idUsuario;
      if (idABorrar === miId) { this.mensajeErrorBorrado = "No puedes borrarte a ti mismo."; return; }
      const updateData = { ...this.usuarioABorrar, activo: false, DNI: (this.usuarioABorrar as any).DNI || (this.usuarioABorrar as any).dni };
      this.usuarioService.actualizarUsuarioConEjecutor(idABorrar, updateData, miId).subscribe({ next: () => { this.mostrarModalBorrado = false; this.cargarDatosSincronizados(); }, error: () => alert('Error') });
    }

    cerrarModalBorrado(): void { this.mostrarModalBorrado = false; this.mensajeErrorBorrado = null; }
    logout(): void { localStorage.removeItem('rol'); }
    toggleMenu(): void { this.isMenuOpen = !this.isMenuOpen; }
    @HostListener('document:click', ['$event'])
    clickout(event: any) { if (!event.target.closest('.user-menu-container')) this.isMenuOpen = false; }

    filtrarConcursos(): void {
    this.concursosFiltrados = this.concursos.filter(c => {
      const cumpleNombre = c.nombre?.toLowerCase().includes(this.terminoBusquedaConcurso.toLowerCase());
      const cumpleEstado = this.filtroEstadoConcurso === '' || c.estadoConcurso === this.filtroEstadoConcurso;
      const cumpleOrg = this.filtroOrgConcurso === '' || c.nombreOrganizacion === this.filtroOrgConcurso;
      
      return cumpleNombre && cumpleEstado && cumpleOrg;
    });

    this.paginaActualConcursos = 1; // Reiniciar a la primera página al filtrar
    this.actualizarPaginacionConcursos();
  }

  actualizarPaginacionConcursos(): void {
    this.totalPaginasConcursos = Math.ceil(this.concursosFiltrados.length / this.itemsPorPaginaConcursos);
    const inicio = (this.paginaActualConcursos - 1) * this.itemsPorPaginaConcursos;
    const fin = inicio + this.itemsPorPaginaConcursos;
    this.concursosPaginados = this.concursosFiltrados.slice(inicio, fin);
  }

  // Al pulsar "Nuevo Concurso"
  crearNuevoConcurso(): void {
    this.modoFormularioConcurso = 'crear';
    this.concursoSeleccionado = {
      nombre: '',
      estadoConcurso: 'ACTIVO',
      id_organizacion: localStorage.getItem('id_organizacion') // Auto-asignar org si no es SYSADMIN
    };
    this.mostrandoFormularioConcurso = true;
  }

  // Al pulsar el icono del OJO
  verDetallesConcurso(concurso: any): void {
    
  }

  // Al pulsar el icono del LÁPIZ
  editarConcurso(concurso: any): void {
    this.modoFormularioConcurso = 'editar';
    this.concursoSeleccionado = { ...concurso };
    this.mostrandoFormularioConcurso = true;
  }

  eliminarConcurso(concurso: any): void {
    const confirmacion = confirm(`¿ESTÁS SEGURO? Esta acción borrará el concurso "${concurso.nombre}" de forma permanente de la base de datos.`);
    
    if (confirmacion) {
      this.concursoService.eliminarConcurso(concurso.idConcurso).subscribe({
        next: () => {
          // Borramos del array local para que desaparezca de la vista inmediatamente
          this.concursos = this.concursos.filter(c => c.idConcurso !== concurso.idConcurso);
          this.cargarDatosSincronizados();
          alert('Concurso eliminado del sistema.');
        },
        error: (err) => {
          // Mostramos el error específico del backend (ej: "No se puede borrar: tiene inscripciones")
          const msg = err.error?.message || 'Error al intentar eliminar el concurso.';
          alert(msg);
        }
      });
    }
  }

  // Para cerrar el formulario (necesitarás un botón de cancelar)
  cerrarFormularioConcurso(): void {
    this.mostrandoFormularioConcurso = false;
  }

    // Abre el modal de confirmación específico para concurso
  abrirModalBorradoConcurso(concurso: any): void {
    this.idConcursoABorrar = concurso.idConcurso;
    this.esErrorModalBorradoConcurso = false;
    this.tituloModalBorradoConcurso = 'Confirmar Eliminación de Concurso';
    this.mensajeModalBorradoConcurso = `¿Estás seguro de que deseas eliminar permanentemente el concurso "${concurso.nombre}"?`;
    this.mostrarModalBorradoConcurso = true;
  }

  // Ejecuta la llamada al servicio para concurso
  confirmarBorradoConcurso(): void {
    if (this.idConcursoABorrar) {
      this.concursoService.eliminarConcurso(this.idConcursoABorrar).subscribe({
        next: () => {
          this.concursos = this.concursos.filter(c => c.idConcurso !== this.idConcursoABorrar);
          this.cerrarModalBorradoConcurso();
        },
        error: (err) => {
          // Transformamos el modal en una ventana de error específica
          this.esErrorModalBorradoConcurso = true;
          this.tituloModalBorradoConcurso = 'Error al eliminar concurso';
          this.mensajeModalBorradoConcurso = err.error?.message || 'El concurso tiene agrupaciones asociadas y no puede ser borrado.';
        }
      });
    }
  }

  cerrarModalBorradoConcurso(): void {
    this.mostrarModalBorradoConcurso = false;
    this.idConcursoABorrar = null;
  }  
}