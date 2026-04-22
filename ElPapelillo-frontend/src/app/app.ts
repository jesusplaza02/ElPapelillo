import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router'; 
import { filter } from 'rxjs/operators';
import { HeaderComponent } from "./componentes/header/header";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('ElPapelillo-frontend');
  
  esRutaAcceso = false;  
  menuAbierto = false;   
  
  // Variables para mostrar los datos del usuario en el Header
  usuarioEmail: string | null = '';
  nombreUsuario: string | null = '';

  confettis: any[] = [];
  colors = ['#FF8A80', '#FF80AB', '#B39DDB', '#82B1FF', '#B9F6CA', '#FFFF8D'];

  constructor(private router: Router, private cdr: ChangeDetectorRef) {
    // Detectamos la ruta actual
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      const url = event.urlAfterRedirects;
      this.esRutaAcceso = url.includes('login') || url.includes('registro');
      
      // Cada vez que cambie la ruta, intentamos recargar los datos del usuario
      // Esto sirve para que al entrar tras el login, el email aparezca
      this.cargarDatosSesion();
    });
  }

  ngOnInit() {
    this.generateConfetti();
    this.cargarDatosSesion();
  }

  // Carga los datos que guardamos en LoginComponent.ts
  cargarDatosSesion() {
    this.usuarioEmail = localStorage.getItem('email');
    this.nombreUsuario = localStorage.getItem('nombreUsuario');
    this.cdr.detectChanges(); // Forzamos a que el HTML vea los cambios
  }

  toggleMenu() {
    this.menuAbierto = !this.menuAbierto;
  }

  // Cerrar sesión completo
  logout() {
    localStorage.clear(); // Borramos el ID, el Email y el Rol
    this.usuarioEmail = '';
    this.nombreUsuario = '';
    this.menuAbierto = false;
    this.router.navigate(['/login']);
  }

  redirigirAlPanel() {
  // 1. Leemos el rol que guardamos en el login
  const rol = localStorage.getItem('rolUsuario');
  
  // LOG de control para que veas en la consola qué está pasando
  console.log("Intentando redirigir. Rol en memoria:", rol);

  if (!rol) {
    // Si no hay rol, es que no está logueado o hubo un error, al login de cabeza
    this.router.navigate(['/login']);
    return;
  }

  const rolLimpio = rol.trim().toUpperCase();

  // 2. Navegación según el rol
  if (rolLimpio === 'ADMINISTRADOR') {
    this.router.navigate(['/panel-control-administrador']);
  } else if (rolLimpio === 'REPRESENTANTE') {
    this.router.navigate(['/panel-representante']);
  } else {
    // Por si acaso el rol es algo raro, vamos al login
    console.warn("Rol no reconocido:", rolLimpio);
    this.router.navigate(['/login']);
  }
}

  // ESTA ES LA QUE TE FALTA PARA "MI PERFIL"
  irAEditar() {
    const id = localStorage.getItem('idUsuario');
    console.log("Intentando editar usuario con ID:", id);
    
    if (id) {
      // Cerramos el menú antes de irnos
      // (si tienes una variable menuAbierto = false; ponla aquí)
      this.router.navigate(['/editar-usuario', id]);
    } else {
      console.error("No se encontró el ID del usuario en el storage");
    }
  }

  generateConfetti() {
    for (let i = 0; i < 100; i++) {
      this.confettis.push({
        style: {
          left: Math.random() * 100 + 'vw',
          top: Math.random() * 100 + 'vh',
          'background-color': this.colors[Math.floor(Math.random() * this.colors.length)],
          transform: `rotate(${Math.random() * 360}deg)`,
          'border-radius': Math.random() > 0.5 ? '50%' : '0'
        }
      });
    }
  }

  
}