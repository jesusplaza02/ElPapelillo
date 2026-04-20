import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { Usuario } from './panel-control-administrador-usuario.model'; 
import { PanelControlAdministradorUsuarioService } from './panel-control-administrador-usuario.service';
import { PanelControlAdministradorAuditoriaService } from './panel-control-administrador-auditoria.service';

@Component({
  selector: 'app-panel-control-administrador',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './panel-control-administrador.html',
  styleUrl: './panel-control-administrador.css'
})
export class PanelControlAdministradorComponent implements OnInit {
  isMenuOpen = false;
  
  // VARIABLES DE TABLA Y BÚSQUEDA
  usuarios: Usuario[] = [];
  usuariosFiltrados: Usuario[] = [];
  terminoBusqueda: string = '';

  // VARIABLES DE AUDITORÍA
  logs: any[] = [];
  logsFiltrados: any[] = [];
  filtroCategoria: string = 'Todos';
  filtroFecha: string = '';

  rolSesionActual: string | null = '';
  
  // VARIABLES DEL FORMULARIO
  mostrandoFormulario = false;
  modoFormulario: 'crear' | 'editar' | 'ver' = 'crear';
  nuevoUsuario: any = {
    nombre: '', email: '', dni: '', direccion: '', 
    telefono: '', rol: 'REPRESENTANTE', contacto_emergencia: '', cargo: '', activo: true 
  };

  // VARIABLES DEL MODAL DE BORRADO
  mostrarModalBorrado = false;
  usuarioABorrar: any = null;

  constructor(
    private usuarioService: PanelControlAdministradorUsuarioService,
    private auditoriaService: PanelControlAdministradorAuditoriaService
  ) {}

  ngOnInit(): void {
    this.rolSesionActual = localStorage.getItem('rol'); 
    this.cargarDatosSincronizados();
  }

  // --- MÉTODOS RESTAURADOS DE PERMISOS ---
  get rolesDisponibles(): string[] {
    const miRol = this.rolSesionActual?.toUpperCase();
    return miRol === 'SUPERADMIN' ? ['REPRESENTANTE', 'ADMINISTRADOR'] : ['REPRESENTANTE'];
  }

  puedeGestionar(u: Usuario): boolean {
    const rolFila = u.rol?.toUpperCase(); 
    const miRol = this.rolSesionActual?.toUpperCase();
    return rolFila === 'ADMINISTRADOR' ? miRol === 'SUPERADMIN' : true;
  }

  cargarDatosSincronizados(): void {
    forkJoin({
      usuariosResponse: this.usuarioService.getUsuarios(),
      logsResponse: this.auditoriaService.getLogs()
    }).subscribe({
      next: (resultados) => {
        this.usuarios = resultados.usuariosResponse.filter((u: any) => u.activo !== 0 && u.activo !== false);
        this.usuariosFiltrados = [...this.usuarios];

        this.logs = resultados.logsResponse.map(log => {
          const fechaObj = new Date(log.fecha);
          const identificador = log.idUsuario || log.id_usuario || log.usuario_id;
          return {
            tipo: this.mapearTipoLog(log.accion), 
            icon: this.mapearIconoLog(log.accion), 
            msg: log.descripcion,
            idUsuario: identificador, 
            fecha: fechaObj.toLocaleDateString('es-ES'),
            fechaIso: fechaObj.toISOString().split('T')[0],
            time: fechaObj.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }),
            cat: this.mapearCategoria(log.accion)
          };
        });
        this.logsFiltrados = [...this.logs];
      },
      error: (err) => console.error("Error al cargar:", err)
    });
  }

  // --- LÓGICA DE BÚSQUEDA ---
  buscarUsuarios(): void {
    const termino = this.terminoBusqueda.toLowerCase().trim();
    if (!termino) {
      this.usuariosFiltrados = [...this.usuarios];
      return;
    }
    this.usuariosFiltrados = this.usuarios.filter(u => {
      const user = u as any; 
      return (user.nombre && user.nombre.toLowerCase().includes(termino)) ||
             (user.email && user.email.toLowerCase().includes(termino)) ||
             (user.dni && user.dni.toLowerCase().includes(termino)) ||
             (user.DNI && user.DNI.toLowerCase().includes(termino));
    });
  }

  // --- LÓGICA DE FORMULARIO ---
  abrirModalRegistro(): void {
    this.modoFormulario = 'crear';
    this.resetForm();
    this.mostrandoFormulario = true;
  }

  verDetalles(u: Usuario): void {
    this.modoFormulario = 'ver';
    this.prepararUsuarioParaFormulario(u);
    this.mostrandoFormulario = true;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  editar(u: Usuario): void {
    this.modoFormulario = 'editar';
    this.prepararUsuarioParaFormulario(u);
    this.mostrandoFormulario = true;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  private prepararUsuarioParaFormulario(u: Usuario): void {
    this.nuevoUsuario = { ...u };
    const userAny = u as any; 
    const rol = userAny.rol?.toUpperCase() || '';
    this.nuevoUsuario.rol = rol;

    if (rol === 'REPRESENTANTE') {
      this.nuevoUsuario.contacto_emergencia = userAny.contacto_emergencia || userAny.telefono_emergencia || '';
    } else if (rol === 'ADMINISTRADOR') {
      this.nuevoUsuario.cargo = userAny.cargo || '';
    }
  }

  guardarFormulario(): void {
    if (this.modoFormulario === 'crear') {
      this.usuarioService.crearUsuario(this.nuevoUsuario).subscribe({
        next: () => { this.cargarDatosSincronizados(); this.mostrandoFormulario = false; },
        error: () => alert('Error al crear')
      });
    } else if (this.modoFormulario === 'editar') {
      this.usuarioService.actualizarUsuario(this.nuevoUsuario).subscribe({
        next: () => { this.cargarDatosSincronizados(); this.mostrandoFormulario = false; },
        error: () => alert('Error al actualizar')
      });
    }
  }

  resetForm(): void {
    this.nuevoUsuario = { 
      nombre: '', email: '', dni: '', direccion: '', 
      telefono: '', rol: 'REPRESENTANTE', contacto_emergencia: '', cargo: '', activo: true 
    };
  }

  // --- LÓGICA DE BORRADO ---
  eliminar(u: Usuario): void {
    this.usuarioABorrar = { ...u };
    this.mostrarModalBorrado = true;
  }

  cerrarModalBorrado(): void { this.mostrarModalBorrado = false; }

  confirmarBorrado(): void {
    if (this.usuarioABorrar) {
      const idAdmin = localStorage.getItem('idUsuario') || '1';
      const updateData = { ...this.usuarioABorrar, activo: false, DNI: this.usuarioABorrar.dni };
      
      this.usuarioService.actualizarUsuarioConEjecutor(this.usuarioABorrar.idUsuario, updateData, Number(idAdmin)).subscribe({
        next: () => { this.cargarDatosSincronizados(); this.cerrarModalBorrado(); },
        error: () => alert('Error al eliminar')
      });
    }
  }

  // --- AUDITORÍA Y UTILIDADES ---
  filtrarLogs(): void {
    this.logsFiltrados = this.logs.filter(log => {
      const cumpleCat = this.filtroCategoria === 'Todos' || log.cat === this.filtroCategoria;
      const cumpleFecha = !this.filtroFecha || log.fechaIso === this.filtroFecha;
      return cumpleCat && cumpleFecha;
    });
  }

  limpiarFiltros(): void {
    this.filtroCategoria = 'Todos'; this.filtroFecha = ''; this.filtrarLogs();
  }

  getNombreResponsable(id: any): string {
    const u = this.usuarios.find(user => user.idUsuario == id);
    return u ? u.nombre : 'Sistema';
  }

  private mapearTipoLog(a: string): string { 
    return a?.includes('BORRADO') ? 'error' : a?.includes('CREAR') ? 'success' : 'info'; 
  }
  private mapearIconoLog(a: string): string { return a?.includes('USUARIO') ? 'person' : 'settings'; }
  private mapearCategoria(a: string): string { return a?.includes('USUARIO') ? 'Usuarios' : 'Sistema'; }

  logout(): void { localStorage.removeItem('rol'); }
  toggleMenu(): void { this.isMenuOpen = !this.isMenuOpen; }
  
  @HostListener('document:click', ['$event'])
  clickout(event: any) { 
    if (!event.target.closest('.user-menu-container')) this.isMenuOpen = false; 
  }
}