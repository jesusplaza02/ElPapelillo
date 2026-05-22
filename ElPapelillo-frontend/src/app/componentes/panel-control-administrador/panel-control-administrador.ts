import { Component, HostListener, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
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
  private router = inject(Router);
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
  logsPaginados: any[] = []; 
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

  // MODAL BORRADO USUARIOS
  mostrarModalBorrado = false;
  usuarioABorrar: any = null;
  mensajeErrorBorrado: string | null = null;

  // ORGANIZACIONES
  organizaciones: any[] = [];
  organizacionesFiltradas: any[] = [];
  terminoBusquedaOrg: string = '';
  mostrandoFormOrg: boolean = false;
  modoFormOrg: 'crear' | 'editar' = 'crear';
  mostrarModalBorradoOrg: boolean = false; 
  orgABorrar: any = null;
  nuevaOrg: any = { 
    idOrganizacion: null, 
    nombre: '', 
    email: '',
    telefono: '', 
    ubicacion: '',
    activo: true 
  };

  // MODAL BORRADO CONCURSOS
  mostrarModalBorradoConcurso = false;
  esErrorModalBorradoConcurso = false;
  mensajeModalBorradoConcurso = '';
  tituloModalBorradoConcurso = '';
  idConcursoABorrar: number | null = null;

  // CONCURSOS STATE
  concursos: any[] = [];
  concursosFiltrados: any[] = [];
  concursosPaginados: any[] = [];
  terminoBusquedaConcurso: string = '';
  filtroEstadoConcurso: string = '';
  filtroOrgConcurso: string = '';

  paginaActualConcursos: number = 1;
  itemsPorPaginaConcursos: number = 5;
  totalPaginasConcursos: number = 1;
  
  // TOAST CONTROL
  mostrarToast: boolean = false;
  mensajeToast: string = '';
  tipoToast: 'success' | 'error' = 'success';

  // MODALES INTERNOS GENERALES (REEMPLAZAN ALERTS)
  mostrarModalExitoGlobal: boolean = false;
  tituloModalExitoGlobal: string = '';
  contenidoModalExitoGlobal: string = '';

  mostrarModalErrorGlobal: boolean = false;
  tituloModalErrorGlobal: string = '';
  contenidoModalErrorGlobal: string = '';

  constructor(
    private usuarioService: PanelControlAdministradorUsuarioService,
    private auditoriaService: PanelControlAdministradorAuditoriaService,
    private organizacionService: PanelControlAdministradorOrganizacionService,
    private concursoService: PanelControlAdministradorConcursoService
  ) {}

  ngOnInit(): void {
    this.rolSesionActual = localStorage.getItem('rol'); 
    this.cargarDatosSincronizados();
    if (this.rolSesionActual?.toUpperCase() === 'SYSADMIN') {
      this.cargarOrganizaciones();
    }
  }

  lanzarToast(mensaje: string, tipo: 'success' | 'error' = 'success'): void {
    this.mensajeToast = mensaje;
    this.tipoToast = tipo;
    this.mostrarToast = true;
    setTimeout(() => {
      this.mostrarToast = false;
    }, 3000);
  }

  lanzarModalInformativo(titulo: string, contenido: string, tipo: 'success' | 'error'): void {
    if (tipo === 'success') {
      this.tituloModalExitoGlobal = titulo;
      this.contenidoModalExitoGlobal = contenido;
      this.mostrarModalExitoGlobal = true;
    } else {
      this.tituloModalErrorGlobal = titulo;
      this.contenidoModalErrorGlobal = contenido;
      this.mostrarModalErrorGlobal = true;
    }
  }

  // --- MÉTODOS DE ORGANIZACIONES ---
  cargarOrganizaciones(): void {
    this.organizacionService.getOrganizaciones().subscribe({
      next: (res) => {
        this.organizaciones = res.filter((o: any) => o.activo !== false && o.activo !== 0);
        this.organizacionesFiltradas = [...this.organizaciones];
      },
      error: () => {
        this.lanzarModalInformativo('Error de datos', 'No se ha podido estructurar el listado de las organizaciones.', 'error');
      }
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
    this.mostrandoFormOrg = false;
    this.mostrarModalBorradoOrg = false;
    this.orgABorrar = null;
    this.nuevaOrg = { idOrganizacion: null, nombre: '', emailContacto: '', telefono: '', direccion: '', activo: true };
    this.cargarOrganizaciones(); 
  }

  guardarOrganizacion(): void {
    const id = this.nuevaOrg.idOrganizacion;
    const peticion = this.modoFormOrg === 'crear' 
      ? this.organizacionService.crearOrganizacion(this.nuevaOrg)
      : this.organizacionService.actualizarOrganizacion(+id, this.nuevaOrg);

    peticion.subscribe({
      next: () => {
        this.lanzarModalInformativo('Éxito', 'La organización se ha guardado correctamente.', 'success');
        this.cerrarTodoOrg();
      },
      error: (err) => {
        if (err.status === 200 || err.status === 201) {
          this.lanzarModalInformativo('Éxito', 'La organización se ha guardado correctamente.', 'success');
          this.cerrarTodoOrg();
        } else {
          this.lanzarModalInformativo('Operación Fallida', 'No se ha podido procesar el registro de la organización.', 'error');
        }
      }
    });
  }

  cerrarYRefrescar(): void {
    this.mostrandoFormOrg = false;
    this.cargarOrganizaciones();
    this.nuevaOrg = { idOrganizacion: null, nombre: '', emailContacto: '', telefono: '', direccion: '', activo: true };
  }

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
          this.mostrarModalBorradoOrg = false;
          this.cargarDatosSincronizados(); 
          this.cargarOrganizaciones();     
          this.lanzarModalInformativo('Eliminada', 'La organización ha sido desactivada en el sistema.', 'success');
        },
        error: () => this.lanzarModalInformativo('Conflicto de eliminación', 'No se puede dar de baja debido a dependencias activas.', 'error')
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
        const activos = usuariosRes.filter((u: any) => u.activo !== 0 && u.activo !== false);
        this.usuarios = miRol === 'SYSADMIN' 
          ? activos 
          : activos.filter((u: any) => {
              const rolFila = u.rol?.toUpperCase();
              return (rolFila === 'SYSADMIN' || rolFila === 'REPRESENTANTE' || Number(u.id_organizacion) === Number(miIdOrg));
            });
        this.usuariosFiltrados = [...this.usuarios];

        this.logs = auditoriaRes.map((log: any) => {
          const fechaObj = new Date(log.fecha);
          return {
            ...log,
            icon: this.mapearIconoLog(log.accion), 
            tipo: this.mapearTipoLog(log.accion),  
            msg: log.accion,                      
            cat: this.mapearCategoria(log.accion), 
            fecha: fechaObj.toLocaleDateString(),  
            time: fechaObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }), 
            fechaIso: log.fecha ? log.fecha.split('T')[0] : ''
          };
        });

        if (miRol !== 'SYSADMIN') {
          this.logs = this.logs.filter(log => this.usuarios.some(u => (u as any).idUsuario == log.idUsuario));
        }
        this.logsFiltrados = [...this.logs];

        this.concursos = concursosRes;
        this.concursosFiltrados = [...this.concursos];

        this.actualizarPaginacion();
      },
      error: () => {
        this.lanzarModalInformativo('Error crítico', 'Error en la sincronización de datos con el servidor.', 'error');
      }
    });
  }

  actualizarPaginacion(): void {
    this.totalPaginas = Math.ceil(this.usuariosFiltrados.length / this.itemsPorPagina) || 1;
    if (this.paginaActual > this.totalPaginas) this.paginaActual = 1;
    const inicioU = (this.paginaActual - 1) * this.itemsPorPagina;
    this.usuariosPaginados = this.usuariosFiltrados.slice(inicioU, inicioU + this.itemsPorPagina);

    this.totalPaginasLogs = Math.ceil(this.logsFiltrados.length / this.itemsPorPagina) || 1;
    if (this.paginaActualLogs > this.totalPaginasLogs) this.paginaActualLogs = 1;
    const inicioL = (this.paginaActualLogs - 1) * this.itemsPorPagina;
    this.logsPaginados = this.logsFiltrados.slice(inicioL, inicioL + this.itemsPorPagina);
  
    this.totalPaginasConcursos = Math.ceil(this.concursosFiltrados.length / this.itemsPorPaginaConcursos) || 1;
    if (this.paginaActualConcursos > this.totalPaginasConcursos) this.paginaActualConcursos = 1;
    const inicioC = (this.paginaActualConcursos - 1) * this.itemsPorPaginaConcursos;
    this.concursosPaginados = this.concursosFiltrados.slice(inicioC, inicioC + this.itemsPorPaginaConcursos);
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

  abrirModalRegistro(): void { this.modoFormulario = 'crear'; this.resetForm(); this.mostrandoFormulario = true; }
  verDetalles(u: Usuario): void { this.modoFormulario = 'ver'; this.prepararUsuarioParaFormulario(u); this.mostrandoFormulario = true; window.scrollTo({ top: 0, behavior: 'smooth' }); }
  editar(u: Usuario): void { this.modoFormulario = 'editar'; this.prepararUsuarioParaFormulario(u); this.mostrandoFormulario = true; window.scrollTo({ top: 0, behavior: 'smooth' }); }
  private prepararUsuarioParaFormulario(u: Usuario): void { this.nuevoUsuario = { ...u }; const rol = (u as any).rol?.toUpperCase() || ''; this.nuevoUsuario.rol = rol; }
  
  guardarFormulario(): void {
    const miRol = this.rolSesionActual?.toUpperCase();
    const miIdOrg = localStorage.getItem('id_organizacion');
    const idEjecutor = Number(localStorage.getItem('idUsuario'));
    const usuarioParaEnviar: any = { ...this.nuevoUsuario };
    const rolLimpio = usuarioParaEnviar.rol?.toUpperCase();
    
    if (rolLimpio === 'REPRESENTANTE') {
      usuarioParaEnviar.type = 'representante';
    } else if (rolLimpio === 'ADMINISTRADOR' || rolLimpio === 'SUPERADMIN') {
      usuarioParaEnviar.type = 'administrador';
    } else {
      delete usuarioParaEnviar.type; 
    }

    if (this.modoFormulario === 'crear') {
      if (miRol === 'SUPERADMIN' && rolLimpio === 'ADMINISTRADOR') {
        usuarioParaEnviar.id_organizacion = miIdOrg;
      }

      this.usuarioService.crearUsuario(usuarioParaEnviar).subscribe({
        next: () => this.finalizarGuardado('Usuario registrado correctamente.'),
        error: (err) => this.manejarError(err)
      });

    } else if (this.modoFormulario === 'editar') {
      const idAEditar = usuarioParaEnviar.idUsuario || usuarioParaEnviar.id;

      if (!idAEditar) {
        this.lanzarModalInformativo('Identificador Erróneo', 'No se ha podido localizar el identificador de este usuario.', 'error');
        return;
      }

      this.usuarioService.actualizarUsuarioConEjecutor(idAEditar, usuarioParaEnviar, idEjecutor).subscribe({
        next: () => this.finalizarGuardado('El usuario se ha actualizado correctamente.'),
        error: (err) => this.manejarError(err)
      });
    }
  }

  // --- MÉTODOS DE CONCURSOS ---
  private formatearFechaInput(fechaStr: string): string {
    if (!fechaStr) return '';
    const d = new Date(fechaStr);
    if (isNaN(d.getTime())) return '';
    return d.toISOString().split('T')[0];
  }

  editarConcurso(concurso: any): void {
    this.modoFormularioConcurso = 'editar';
    
    // Sincronizamos las propiedades asegurando el formato YYYY-MM-DD para los <input type="date">
    this.concursoSeleccionado = { 
      ...concurso,
      fechaInicio: this.formatearFechaInput(concurso.fechaInicio || concurso.fecha_inicio),
      fechaFin: this.formatearFechaInput(concurso.fechaFin || concurso.fecha_fin),
      fechaInicioInscripcion: this.formatearFechaInput(concurso.fechaInicioInscripcion || concurso.fecha_inicio_inscripcion),
      fechaFinInscripcion: this.formatearFechaInput(concurso.fechaFinInscripcion || concurso.fecha_fin_inscripcion)
    };
    
    // CRÍTICO: Activamos la vista del formulario
    this.mostrandoFormularioConcurso = true;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  guardarConcurso(): void {
  const c = { ...this.concursoSeleccionado }; // Clonamos para evitar mutaciones directas en la vista

  if (this.rolSesionActual !== 'SYSADMIN') {
    const idOrgPropia = localStorage.getItem('id_organizacion') || localStorage.getItem('idOrganizacion');
    if (idOrgPropia) c.id_organizacion = parseInt(idOrgPropia);
  }

  if (!c.id_organizacion) {
    this.lanzarModalInformativo('Falta Organización', 'Es obligatorio asociar una organización válida.', 'error');
    return;
  }

  // Validaciones de fechas seguras sin romper el formato string de la vista
  if (c.fechaInicio && c.fechaFinInscripcion) {
    const fIniCon = new Date(c.fechaInicio);
    const fFinIns = new Date(c.fechaFinInscripcion);
    if (fFinIns > fIniCon) {
      this.lanzarModalInformativo('Fechas incorrectas', 'El plazo de inscripción debe finalizar antes del inicio del concurso.', 'error');
      return;
    }
  }

  const idConcurso = c.idConcurso || c.id;
  const servicioCall = this.modoFormularioConcurso === 'crear' 
    ? this.concursoService.crearConcurso(c)
    : this.concursoService.actualizarConcurso(idConcurso, c);

  servicioCall.subscribe({
    next: (concursoActualizado: any) => {
      // 🚀 TRUCO CLAVE: Actualizamos el concurso directamente en la lista local 
      // para que la tabla reaccione INSTANTÁNEAMENTE en el primer clic.
      if (this.modoFormularioConcurso === 'editar') {
        const index = this.concursos.findIndex(item => (item.idConcurso || item.id) === idConcurso);
        if (index !== -1) {
          // Fusionamos los datos antiguos con los nuevos editados
          this.concursos[index] = { ...this.concursos[index], ...c };
        }
      }

      // Procedemos a cerrar el formulario y refrescar todo el panel
      this.finalizarGuardado('El concurso se ha configurado de manera idónea.');
    },
    error: (err) => {
      console.error(err);
      this.lanzarModalInformativo('Error del Servidor', 'Verifique los campos obligatorios del concurso.', 'error');
    }
  });
}

  private finalizarGuardado(mensaje: string): void {
    this.lanzarModalInformativo('Operación Completada', mensaje, 'success');
    this.mostrandoFormulario = false;
    this.mostrandoFormularioConcurso = false;
    this.mostrandoFormOrg = false;
    this.cargarDatosSincronizados();
    if (this.rolSesionActual?.toUpperCase() === 'SYSADMIN') {
      this.cargarOrganizaciones();
    }
  }

  private manejarError(err: any): void {
    const msg = err.error?.message || err.error || 'No se pudo realizar el proceso solicitado.';
    this.lanzarModalInformativo('Aviso de Error', msg, 'error');
  }

  resetForm(): void { 
    this.nuevoUsuario = { nombre: '', email: '', dni: '', direccion: '', telefono: '', rol: 'REPRESENTANTE', contacto_emergencia: '', cargo: '', activo: true }; 
  }

  eliminar(u: Usuario): void { 
    this.usuarioABorrar = { ...u }; 
    this.mensajeErrorBorrado = null; 
    this.mostrarModalBorrado = true; 
  }
    
  confirmarBorrado(): void {
    if (!this.usuarioABorrar || !this.usuarioABorrar.idUsuario) return;
    
    const miId = Number(localStorage.getItem('idUsuario'));
    const idABorrar = this.usuarioABorrar.idUsuario;

    this.usuarioService.eliminarUsuario(idABorrar, miId).subscribe({
      next: () => {
        this.mostrarModalBorrado = false; 
        this.usuarioABorrar = null;
        this.cargarDatosSincronizados();
        this.lanzarModalInformativo('Usuario Desactivado', 'Se ha inhabilitado el usuario con éxito.', 'success');
      },
      error: (err) => {
        this.mostrarModalBorrado = false;
        const mensajeError = typeof err.error === 'string' ? err.error : 'No está permitido eliminar tu propio usuario de sesión activo.';
        this.lanzarModalInformativo('Restricción', mensajeError, 'error');
      }
    });
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
    this.paginaActualConcursos = 1; 
    this.actualizarPaginacionConcursos();
  }

  actualizarPaginacionConcursos(): void {
    this.totalPaginasConcursos = Math.ceil(this.concursosFiltrados.length / this.itemsPorPaginaConcursos) || 1;
    const inicio = (this.paginaActualConcursos - 1) * this.itemsPorPaginaConcursos;
    const fin = inicio + this.itemsPorPaginaConcursos;
    this.concursosPaginados = this.concursosFiltrados.slice(inicio, fin);
  }

  crearNuevoConcurso(): void {
    this.modoFormularioConcurso = 'crear';
    this.concursoSeleccionado = {
      nombre: '',
      tipoConcurso: '',
      estadoConcurso: 'ACTIVO',
      id_organizacion: localStorage.getItem('id_organizacion') || localStorage.getItem('idOrganizacion')
    };
    this.mostrandoFormularioConcurso = true;
  }

  verDetallesConcurso(concurso: any): void {
    const id = concurso.idConcurso || concurso.id;
    if (id) {
      this.router.navigate(['/detalle-concurso', id]);
    } else {
      this.lanzarModalInformativo('Error de Enlace', 'El concurso seleccionado carece de identificador dinámico.', 'error');
    }
  }

  eliminarConcurso(concurso: any): void {
    this.abrirModalBorradoConcurso(concurso);
  }

  cerrarFormularioConcurso(): void {
    this.mostrandoFormularioConcurso = false;
  }

  abrirModalBorradoConcurso(concurso: any): void {
    this.idConcursoABorrar = concurso.idConcurso || concurso.id;
    this.esErrorModalBorradoConcurso = false;
    this.tituloModalBorradoConcurso = 'Confirmar Eliminación de Concurso';
    this.mensajeModalBorradoConcurso = `¿Estás seguro de que deseas eliminar permanentemente el concurso "${concurso.nombre}"?`;
    this.mostrarModalBorradoConcurso = true;
  }

  confirmarBorradoConcurso(): void {
    if (this.idConcursoABorrar) {
      this.concursoService.eliminarConcurso(this.idConcursoABorrar).subscribe({
        next: () => {
          this.cerrarModalBorradoConcurso();
          this.cargarDatosSincronizados(); 
          this.lanzarModalInformativo('Registro Eliminado', 'El concurso se ha eliminado de forma permanente.', 'success');
        },
        error: (err) => {
          this.esErrorModalBorradoConcurso = true;
          this.mensajeModalBorradoConcurso = err.error?.message || 'Existen participantes o registros vinculados a las bases de este concurso.';
        }
      });
    }
  }

  cerrarModalBorradoConcurso(): void {
    this.mostrarModalBorradoConcurso = false;
    this.idConcursoABorrar = null;
  }  
}