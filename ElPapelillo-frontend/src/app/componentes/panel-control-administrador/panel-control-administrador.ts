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
  usuarios: Usuario[] = [];
  logs: any[] = [];
  logsFiltrados: any[] = [];
  rolSesionActual: string | null = '';
  
  // VARIABLES DEL FORMULARIO
  mostrandoFormulario = false;
  modoFormulario: 'crear' | 'editar' | 'ver' = 'crear';
  
  // VARIABLES DE FILTROS DE AUDITORÍA
  filtroCategoria: string = 'Todos';
  filtroFecha: string = '';

  // VARIABLES DEL MODAL DE BORRADO
  mostrarModalBorrado = false;
  usuarioABorrar: any = null;

  nuevoUsuario: any = {
    nombre: '', email: '', dni: '', direccion: '', 
    telefono: '', rol: 'REPRESENTANTE', contacto_emergencia: '', cargo: '', activo: true 
  };

  constructor(
    private usuarioService: PanelControlAdministradorUsuarioService,
    private auditoriaService: PanelControlAdministradorAuditoriaService
  ) {}

  ngOnInit(): void {
    this.rolSesionActual = localStorage.getItem('rol'); 
    this.cargarDatosSincronizados();
  }

  get rolesDisponibles(): string[] {
    const miRol = this.rolSesionActual?.toUpperCase();
    return miRol === 'SUPERADMIN' ? ['REPRESENTANTE', 'ADMINISTRADOR'] : ['REPRESENTANTE'];
  }

  cargarDatosSincronizados(): void {
    forkJoin({
      usuariosResponse: this.usuarioService.getUsuarios(),
      logsResponse: this.auditoriaService.getLogs()
    }).subscribe({
      next: (resultados) => {
        // Filtramos para que la tabla solo muestre los activos
        this.usuarios = resultados.usuariosResponse.filter((u: any) => u.activo !== 0 && u.activo !== false);

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

  // --- LÓGICA DE BOTONES Y FORMULARIO ---

  abrirModalRegistro(): void {
    this.modoFormulario = 'crear';
    this.mostrandoFormulario = !this.mostrandoFormulario;
    if (!this.mostrandoFormulario) this.resetForm();
  }

  verDetalles(u: Usuario): void {
    this.modoFormulario = 'ver';
    this.nuevoUsuario = { ...u };
    this.mostrandoFormulario = true;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  editar(u: Usuario): void {
    this.modoFormulario = 'editar';
    this.nuevoUsuario = { ...u }; 
    this.mostrandoFormulario = true;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  guardarFormulario(): void {
    if (this.modoFormulario === 'crear') {
      console.log("Creando usuario:", this.nuevoUsuario);
      this.usuarioService.crearUsuario(this.nuevoUsuario).subscribe({
        next: (usuarioCreado) => {
          this.usuarios.push(usuarioCreado); 
          this.mostrandoFormulario = false;
          this.resetForm();
        },
        error: (err) => {
          console.error("Error al crear:", err);
          alert('Error al crear el usuario');
        }
      });
    } else if (this.modoFormulario === 'editar') {
      console.log("Actualizando usuario:", this.nuevoUsuario);
      this.usuarioService.actualizarUsuario(this.nuevoUsuario).subscribe({
        next: (usuarioActualizado) => {
          const index = this.usuarios.findIndex(u => u.idUsuario === usuarioActualizado.idUsuario);
          if (index !== -1) {
            this.usuarios[index] = usuarioActualizado;
          }
          this.mostrandoFormulario = false;
          this.resetForm();
        },
        error: (err) => {
          console.error("Error al actualizar:", err);
          alert('Error al actualizar el usuario');
        }
      });
    }
  }

  resetForm(): void {
    this.nuevoUsuario = { 
      nombre: '', email: '', dni: '', direccion: '', 
      telefono: '', rol: 'REPRESENTANTE', contacto_emergencia: '', cargo: '', activo: true 
    };
  }

  // --- LÓGICA DEL MODAL DE BORRADO ---

  eliminar(u: Usuario): void {
    this.usuarioABorrar = { ...u }; // Creamos un clon para evitar modificar la tabla visual antes de enviar a BD
    this.mostrarModalBorrado = true;
  }

  cerrarModalBorrado(): void {
    this.mostrarModalBorrado = false;
    this.usuarioABorrar = null;
  }

  confirmarBorrado(): void {
  if (this.usuarioABorrar) {
    // 1. Limpiamos el ID: nos aseguramos de que sea SOLO un número (ej: 1, no "1:1")
    const idLimpio = String(this.usuarioABorrar.idUsuario).split(':')[0];
    const idAdminLogueado = localStorage.getItem('idUsuario') || '1';

    // 2. Preparamos el objeto exacto que Java espera (con MAYÚSCULAS)
    const datosParaEnviar = {
      ...this.usuarioABorrar,
      idUsuario: Number(idLimpio), // Forzamos que sea número
      activo: false,               // Borrado lógico
      DNI: this.usuarioABorrar.dni, // Mapeo para Java
      email: this.usuarioABorrar.email
    };

    console.log('Enviando borrado lógico para ID:', idLimpio);

    // 3. Usamos el método que ya tienes en el servicio
    this.usuarioService.actualizarUsuarioConEjecutor(
      Number(idLimpio), 
      datosParaEnviar, 
      Number(idAdminLogueado)
    ).subscribe({
      next: () => {
        console.log('Borrado éxito en BD');
        // Quitamos de la tabla visualmente
        this.usuarios = this.usuarios.filter(u => String(u.idUsuario).split(':')[0] !== idLimpio);
        this.cerrarModalBorrado();
      },
      error: (err) => {
        console.error('Error 400 detectado:', err);
        alert('Error: El servidor no reconoce la petición. Revisa los logs.');
      }
    });
  }
}

  // --- LÓGICA DE FILTROS DE AUDITORÍA ---

  filtrarLogs(): void {
    this.logsFiltrados = this.logs.filter(log => {
      const cumpleCategoria = this.filtroCategoria === 'Todos' || log.cat === this.filtroCategoria;
      const cumpleFecha = !this.filtroFecha || log.fechaIso === this.filtroFecha;
      return cumpleCategoria && cumpleFecha;
    });
  }

  limpiarFiltros(): void {
    this.filtroCategoria = 'Todos'; 
    this.filtroFecha = ''; 
    this.filtrarLogs();
  }

  // --- OTRAS FUNCIONES ---

  puedeGestionar(u: Usuario): boolean {
    const rolFila = u.rol?.toUpperCase(); const miRol = this.rolSesionActual?.toUpperCase();
    return rolFila === 'ADMINISTRADOR' ? miRol === 'SUPERADMIN' : true;
  }

  getNombreResponsable(idUsuario: any): string {
    if (!idUsuario) return 'Sistema';
    const usuario = this.usuarios.find(u => u.idUsuario == idUsuario);
    return usuario ? usuario.nombre : 'Sistema';
  }

  private mapearTipoLog(accion: string): string {
    const a = accion?.toUpperCase() || '';
    if (a.includes('BORRADO') || a.includes('ERROR')) return 'error';
    if (a.includes('APROBACIÓN') || a.includes('CREAR')) return 'success';
    return 'info';
  }
  
  private mapearIconoLog(accion: string): string {
    const a = accion?.toUpperCase() || '';
    if (a.includes('USUARIO')) return 'person';
    if (a.includes('DOC')) return 'description';
    return 'settings';
  }
  
  private mapearCategoria(accion: string): string {
    const a = accion?.toUpperCase() || '';
    if (a.includes('USUARIO')) return 'Usuarios';
    if (a.includes('DOC')) return 'Documentación';
    if (a.includes('CONCURSO')) return 'Concursos';
    return 'Sistema';
  }

  logout(): void { localStorage.removeItem('rol'); }
  toggleMenu(): void { this.isMenuOpen = !this.isMenuOpen; }
  
  @HostListener('document:click', ['$event'])
  clickout(event: any) { 
    if (!event.target.closest('.user-menu-container')) this.isMenuOpen = false; 
  }
}