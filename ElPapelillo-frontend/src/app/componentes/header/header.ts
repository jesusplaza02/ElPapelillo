import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class HeaderComponent implements OnInit {
  usuarioEmail: string | null = '';
  nombreUsuario: string | null = '';

  constructor(private router: Router) {}

  ngOnInit() {
    // Recuperamos los datos guardados en el LoginComponent
    this.cargarDatosUsuario();
  }

  cargarDatosUsuario() {
    this.usuarioEmail = localStorage.getItem('email');
    this.nombreUsuario = localStorage.getItem('nombreUsuario');
  }

  // Verifica si hay una sesión activa para mostrar u ocultar el botón de usuario
  isLoggedIn(): boolean {
    return localStorage.getItem('idUsuario') !== null;
  }

  cerrarSesion() {
    localStorage.clear(); // Borramos todo
    this.usuarioEmail = '';
    this.nombreUsuario = '';
    this.router.navigate(['/login']); // Al login
  }

  irAEditarDatos() {
    const id = localStorage.getItem('idUsuario');
    if (id) {
      this.router.navigate(['/editar-usuario', id]);
    }
  }

  redirigirAlPanel() {
  // 1. Forzamos la lectura del valor actual en el momento del click
  const rol = localStorage.getItem('rolUsuario');
  
  // LOG DE SEGURIDAD: Abre la consola (F12) y dime qué sale aquí
  console.log("VALOR DEL ROL EN STORAGE:", rol);

  if (!rol) {
    console.error("No se encontró el rol. ¿Hiciste login correctamente?");
    this.router.navigate(['/login']);
    return;
  }

  // 2. Limpieza de strings (evita errores por espacios o comillas)
  const rolLimpio = rol.trim().toUpperCase();

  // 3. Navegación
  if (rolLimpio === 'ADMINISTRADOR') {
    this.router.navigate(['panel-control-administrador']);
  } else if (rolLimpio === 'REPRESENTANTE') {
    this.router.navigate(['/panel-representante']);
  } else {
    console.warn("Rol no coincide con las opciones. Valor:", rolLimpio);
    this.router.navigate(['/login']); // Por defecto al home
  }
}
}