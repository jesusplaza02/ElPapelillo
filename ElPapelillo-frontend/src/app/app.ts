import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router'; // Añadimos Router y NavigationEnd
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('ElPapelillo-frontend');
  
  // Variables para el control de la interfaz
  esRutaAcceso = false;  // Esta es la que te faltaba y daba error
  menuAbierto = false;   // Para el desplegable del perfil
  
  confettis: any[] = [];
  colors = ['#FF8A80', '#FF80AB', '#B39DDB', '#82B1FF', '#B9F6CA', '#FFFF8D'];

  constructor(private router: Router) {
    // Detectamos la ruta actual para ocultar el header en login/registro
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      const url = event.urlAfterRedirects;
      this.esRutaAcceso = url.includes('login') || url.includes('registro');
    });
  }

  ngOnInit() {
    this.generateConfetti();
  }

  // Lógica del menú de usuario
  toggleMenu() {
    this.menuAbierto = !this.menuAbierto;
  }

  logout() {
    this.menuAbierto = false;
    this.router.navigate(['/login']);
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