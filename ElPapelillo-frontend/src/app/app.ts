import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common'; // Para el [ngStyle]
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet], // Quitamos LoginComponent de aquí
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('ElPapelillo-frontend');
  
  // Arregla el error "Property 'confettis' does not exist"
  confettis: any[] = [];
  colors = ['#FF8A80', '#FF80AB', '#B39DDB', '#82B1FF', '#B9F6CA', '#FFFF8D'];

  ngOnInit() {
    this.generateConfetti();
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