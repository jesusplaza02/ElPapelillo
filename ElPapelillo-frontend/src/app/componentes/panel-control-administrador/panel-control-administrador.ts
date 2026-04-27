import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { Usuario } from './panel-control-administrador-usuario.model'; 
import { PanelControlAdministradorUsuarioService } from './panel-control-administrador-usuario.service';
import { PanelControlAdministradorAuditoriaService } from './panel-control-administrador-auditoria.service';
import { PanelControlAdministradorOrganizacionService } from './panel-control-administrador-organizacion.service';

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
  nuevaOrg: any = { idOrganizacion: null, nombre: '', cif: '', emailContacto: '', telefono: '', direccion: '' };

  constructor(
    private usuarioService: PanelControlAdministradorUsuarioService,
    private auditoriaService: PanelControlAdministradorAuditoriaService,
    private organizacionService: PanelControlAdministradorOrganizacionService
  ) {}

  ngOnInit(): void {
    this.rolSesionActual = localStorage.getItem('rol'); 
    this.cargarUsuariosSincronizados();
  }

  cargarUsuariosSincronizados(): void {
    const miRol = (this.rolSesionActual || '').toUpperCase();
    const miIdOrg = localStorage.getItem('id_organizacion');

    forkJoin({
      usuariosRes: this.usuarioService.getUsuarios(),
      auditoriaRes: this.auditoriaService.getLogs()
    }).subscribe({
      next: ({ usuariosRes, auditoriaRes }) => {
        
        // 1. FILTRAR USUARIOS (POR ESTADO Y ORGANIZACIÓN)
        const activos = usuariosRes.filter((u: any) => u.activo !== 0 && u.activo !== false);

        if (miRol === 'SYSADMIN') {
          this.usuarios = activos;
        } else {
          this.usuarios = activos.filter((u: any) => {
            const rolFila = u.rol?.toUpperCase();
            const suIdOrg = u.id_organizacion;
            if (rolFila === 'SYSADMIN' || rolFila === 'REPRESENTANTE') return true;
            return (suIdOrg != null && miIdOrg != null && suIdOrg == miIdOrg);
          });
        }
        this.usuariosFiltrados = [...this.usuarios];

        // 2. FILTRAR Y PROCESAR AUDITORÍA
        // Primero mapeamos para tener nombres, iconos y tipos listos
        // 2. FILTRAR Y PROCESAR AUDITORÍA (RESTAURANDO ESTÉTICA)
          const logsProcesados = auditoriaRes.map((log: any) => {
          const fechaObj = new Date(log.fecha);
          const autor = this.usuarios.find(u => (u as any).idUsuario == log.idUsuario);
          
          return {
            ...log,
            // Estos nombres deben ser EXACTOS a los de tu @for en el HTML
            icon: this.mapearIconoLog(log.accion), 
            tipo: this.mapearTipoLog(log.accion),
            msg: log.accion, // O log.descripcion si prefieres el texto largo
            cat: this.mapearCategoria(log.accion),
            // Formateamos la fecha para que no salga el chorro de texto ISO
            fecha: fechaObj.toLocaleDateString(), 
            time: fechaObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
            fechaIso: log.fecha ? log.fecha.split('T')[0] : '',
            responsableNombre: autor ? autor.nombre : (log.nombreResponsable || 'Sistema')
          };
        });

        // Solo mostramos logs de usuarios que pertenecen a mi organización
        if (miRol === 'SYSADMIN') {
          this.logs = logsProcesados;
        } else {
          this.logs = logsProcesados.filter(log => 
            this.usuarios.some(u => (u as any).idUsuario == log.idUsuario)
          );
        }
        this.logsFiltrados = [...this.logs];

        // 3. EJECUTAR PAGINACIÓN INICIAL
        this.actualizarPaginacion();
      },
      error: (err) => console.error("Error al cargar:", err)
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
      this.usuarioService.crearUsuario(this.nuevoUsuario).subscribe({ next: () => { alert('Usuario creado'); this.cargarUsuariosSincronizados(); this.mostrandoFormulario = false; }, error: () => alert('Error') });
    } else if (this.modoFormulario === 'editar') {
      this.usuarioService.actualizarUsuarioConEjecutor(this.nuevoUsuario.idUsuario, this.nuevoUsuario, idEjecutor).subscribe({ next: () => { alert('Actualizado'); this.cargarUsuariosSincronizados(); this.mostrandoFormulario = false; }, error: () => alert('Error') });
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
    this.usuarioService.actualizarUsuarioConEjecutor(idABorrar, updateData, miId).subscribe({ next: () => { this.mostrarModalBorrado = false; this.cargarUsuariosSincronizados(); }, error: () => alert('Error') });
  }

  cerrarModalBorrado(): void { this.mostrarModalBorrado = false; this.mensajeErrorBorrado = null; }
  logout(): void { localStorage.removeItem('rol'); }
  toggleMenu(): void { this.isMenuOpen = !this.isMenuOpen; }
  @HostListener('document:click', ['$event'])
  clickout(event: any) { if (!event.target.closest('.user-menu-container')) this.isMenuOpen = false; }
}