import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router'; 
import { filter } from 'rxjs/operators';
import { HeaderComponent } from "./componentes/header/header";

@Component({
  selector: 'app-root',
  standalone: true,
  // IMPORTANTE: Asegúrate de incluir HeaderComponent aquí si lo usas en app.html
  imports: [CommonModule, RouterOutlet, HeaderComponent], 
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('ElPapelillo-frontend');
  
  // Control de interfaz global
  esRutaAcceso = false;  
  menuAbierto = false;   

  // Configuración de efectos visuales
  confettis: any[] = [];
  colors = ['#FF8A80', '#FF80AB', '#B39DDB', '#82B1FF', '#B9F6CA', '#FFFF8D'];

  constructor(private router: Router) {
    // Detectamos la ruta para ocultar/mostrar elementos globales si es necesario
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      const url = event.urlAfterRedirects;
      // Si estamos en login o registro, marcamos como ruta de acceso
      this.esRutaAcceso = url.includes('login') || url.includes('registro');
    });
  }

  ngOnInit() {
    this.generateConfetti();
  }

  /**
   * Genera los elementos visuales del fondo
   */
  generateConfetti() {
    this.confettis = []; // Limpiamos antes de generar
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

  /**
   * El resto de funciones (redirigir, cerrar sesión, cargar nombre) 
   * ahora viven en el HeaderComponent para evitar conflictos.
   */
}