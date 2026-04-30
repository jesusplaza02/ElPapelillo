import { Component, OnInit, Input } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class HeaderComponent implements OnInit {
  @Input() ocultarMenu: boolean = false;
  usuarioEmail: string | null = '';
  nombreUsuario: string | null = '';
  menuAbierto: boolean = false;

  constructor(private router: Router) {}

  ngOnInit() {
    // Carga inicial al refrescar la página
    this.cargarDatosUsuario();
    
    // Escucha cambios en el storage por si el login ocurre en otra pestaña
    window.addEventListener('storage', () => this.cargarDatosUsuario());
  }

  /**
   * Carga los datos básicos del storage y limpia comillas residuales
   */

  // AÑADE ESTA FUNCIÓN
  toggleMenu(event: Event) {
    event.stopPropagation(); // Evita que el clic cierre el menú inmediatamente
    this.menuAbierto = !this.menuAbierto;
  }

  // RECOMENDADO: Añadir esto al constructor o ngOnInit para cerrar el menú al hacer clic fuera
  cerrarMenuGlobal() {
    window.onclick = () => {
      this.menuAbierto = false;
    };
  }
  cargarDatosUsuario() {
    const email = localStorage.getItem('email');
    const nombre = localStorage.getItem('nombreUsuario');

    this.usuarioEmail = email ? email.replace(/"/g, '').trim() : '';
    this.nombreUsuario = nombre ? nombre.replace(/"/g, '').trim() : '';
  }

  /**
   * Getter dinámico: Esta es la clave para que el nombre aparezca 
   * siempre actualizado en el HTML sin necesidad de F5.
   */
  get nombreAMostrar(): string {
    const nombre = localStorage.getItem('nombreUsuario');
    const email = localStorage.getItem('email');
    
    if (nombre && nombre !== 'undefined' && nombre !== 'null' && nombre !== '') {
      return nombre.replace(/"/g, '').trim();
    }
    if (email && email !== 'undefined' && email !== 'null' && email !== '') {
      return email.replace(/"/g, '').trim();
    }
    return 'Usuario';
  }

  /**
   * Verifica si hay una sesión activa
   */
  isLoggedIn(): boolean {
    return localStorage.getItem('idUsuario') !== null;
  }

  /**
   * Redirección inteligente según el rol guardado
   */
  redirigirAlPanel() {
    const rol = localStorage.getItem('rolUsuario');
    
    console.log("INTENTANDO REDIRIGIR. ROL DETECTADO:", rol);

    if (!rol) {
      console.warn("Sin rol en memoria, enviando a login...");
      this.router.navigate(['/login']);
      return;
    }

    const rolLimpio = rol.trim().toUpperCase().replace(/"/g, '');

    // Caso Administradores y Sysadmin
    if (['ADMINISTRADOR', 'SUPERADMIN', 'SYSADMIN'].includes(rolLimpio)) {
      console.log("Accediendo a Panel de Control de Administración");
      this.router.navigate(['/panel-control-administrador']);
    } 
    // Caso Representantes
    else if (rolLimpio === 'REPRESENTANTE') {
      console.log("Accediendo a Panel de Representante");
      this.router.navigate(['/panel-representante']);
    } 
    // Otros casos o error
    else {
      console.error("Rol no reconocido o sin permiso:", rolLimpio);
      this.router.navigate(['/home']);
    }
  }

  irAEditarDatos() {
    const id = localStorage.getItem('idUsuario');
    if (id) {
      this.router.navigate(['/editar-usuario', id]);
    }
  }

  cerrarSesion() {
    localStorage.clear(); 
    this.usuarioEmail = '';
    this.nombreUsuario = '';
    this.router.navigate(['/login']);
  }
}