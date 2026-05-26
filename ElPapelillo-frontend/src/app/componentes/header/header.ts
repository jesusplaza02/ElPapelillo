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
    this.cargarDatosUsuario();
    
    window.addEventListener('storage', () => this.cargarDatosUsuario());
  }

  toggleMenu(event: Event) {
    event.stopPropagation(); 
    this.menuAbierto = !this.menuAbierto;
  }

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

  isLoggedIn(): boolean {
    return localStorage.getItem('idUsuario') !== null;
  }

  redirigirAlPanel() {
    const rol = localStorage.getItem('rolUsuario');
    
    console.log("INTENTANDO REDIRIGIR. ROL DETECTADO:", rol);

    if (!rol) {
      console.warn("Sin rol en memoria, enviando a login...");
      this.router.navigate(['/login']);
      return;
    }

    const rolLimpio = rol.trim().toUpperCase().replace(/"/g, '');

    if (['ADMINISTRADOR', 'SUPERADMIN', 'SYSADMIN'].includes(rolLimpio)) {
      console.log("Accediendo a Panel de Control de Administración");
      this.router.navigate(['/panel-control-administrador']);
    } 
    else if (rolLimpio === 'REPRESENTANTE') {
      console.log("Accediendo a Panel de Representante");
      this.router.navigate(['/panel-representante']);
    } 
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