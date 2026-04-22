import { Component, ChangeDetectorRef } from '@angular/core'; // 1. Importamos ChangeDetectorRef
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms'; 
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, HttpClientModule],
  templateUrl: './registro.html',
  styleUrl: './registro.css'
})
export class RegistroComponent {
  
  datosUsuario = {
    email: '',
    nombre: '',
    apellidos: '',
    dni: '',
    direccion: '',
    telefono: '',
    telEmergencia: '',
    rol: 'REPRESENTANTE'
  };

  mensaje: string | null = null;
  esExito: boolean = false;

  constructor(
    private http: HttpClient, 
    private router: Router,
    private cdr: ChangeDetectorRef // 2. Inyectamos el detector de cambios
  ) {}

  finalizarRegistro() {
    this.mensaje = null;

    this.http.post('http://localhost:8080/api/registro', this.datosUsuario)
      .subscribe({
        next: (res: any) => {
          this.mostrarMensaje('✅ ' + (res.message || '¡Registro exitoso!'), true);
          this.cdr.detectChanges(); // Forzamos refresco
          setTimeout(() => this.router.navigate(['/login']), 3000);
        },
        error: (err) => {
          console.error("Error capturado:", err);
          
          let textoError = 'Error al procesar el registro.';
          if (err.error && err.error.message) {
            textoError = err.error.message;
          } else if (typeof err.error === 'string') {
            textoError = err.error;
          }

          this.mostrarMensaje('❌ ' + textoError, false);
          
          // 3. ¡LA CLAVE! Forzamos a Angular a pintar el mensaje AHORA
          this.cdr.detectChanges(); 
        }
      });
  }

  mostrarMensaje(texto: string, exito: boolean) {
    this.mensaje = texto;
    this.esExito = exito;
  }
}