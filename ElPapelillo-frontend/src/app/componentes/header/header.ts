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
}