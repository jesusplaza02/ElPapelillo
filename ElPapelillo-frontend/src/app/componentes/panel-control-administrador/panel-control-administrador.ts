import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

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

  // 2. Añade las variables de estado para concursos
  concursos: any[] = [];
  concursosFiltrados: any[] = [];
  concursosPaginados: any[] = [];
  paginaActualConcursos: number = 1;
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

  forkJoin({
    usuariosRes: this.usuarioService.getUsuarios(),
    auditoriaRes: this.auditoriaService.getLogs()
  }).subscribe({
    next: ({ usuariosRes, auditoriaRes }) => {
      // 1. Usuarios
      const activos = usuariosRes.filter((u: any) => u.activo !== 0 && u.activo !== false);
      this.usuarios = miRol === 'SYSADMIN' 
        ? activos 
        : activos.filter((u: any) => {
            const rolFila = u.rol?.toUpperCase();
            return (rolFila === 'SYSADMIN' || rolFila === 'REPRESENTANTE' || u.id_organizacion == miIdOrg);
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

      // 3. Paginación
      this.actualizarPaginacion();
    }
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
    if (this.modoFormulario === 'crear') {
      if (miRol === 'SUPERADMIN' && this.nuevoUsuario.rol === 'ADMINISTRADOR') this.nuevoUsuario.id_organizacion = miIdOrg; 
      this.usuarioService.crearUsuario(this.nuevoUsuario).subscribe({ next: () => { alert('Usuario creado'); this.cargarDatosSincronizados(); this.mostrandoFormulario = false; }, error: () => alert('Error') });
    } else if (this.modoFormulario === 'editar') {
      this.usuarioService.actualizarUsuarioConEjecutor(this.nuevoUsuario.idUsuario, this.nuevoUsuario, idEjecutor).subscribe({ next: () => { alert('Actualizado'); this.cargarDatosSincronizados(); this.mostrandoFormulario = false; }, error: () => alert('Error') });
    }
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
}