import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router'; 
import { FormsModule } from '@angular/forms'; 
import { HttpClientModule } from '@angular/common/http';
import { AuthService } from './auth.service'; 

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, HttpClientModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent implements OnInit {
  // Datos vinculados al formulario
  loginData = {
    email: '',
    password: ''
  };

  // Estados de la interfaz
  errorMessage: string = '';
  showPassword: boolean = false;

  // Configuración visual
  confettis: any[] = [];
  colors = ['#FFCDD2', '#F8BBD0', '#E1BEE7', '#D1C4E9', '#C5CAE9', '#B3E5FC', '#C8E6C9', '#FFF9C4'];

  constructor(
    private authService: AuthService, 
    private router: Router,
    private cdr: ChangeDetectorRef 
  ) {}

  ngOnInit() {
    this.generateConfetti();
    // Limpiamos cualquier rastro de sesión anterior al cargar el login
    localStorage.clear();
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  /**
   * Método principal de Inicio de Sesión
   */
  onLogin() {
    this.errorMessage = '';
    console.log('Intentando login con:', this.loginData.email);

    if (this.loginData.email && this.loginData.password) {
      this.authService.login(this.loginData).subscribe({
        next: (res: any) => {
          console.log('Respuesta del servidor:', res);

          // 1. Guardamos el ID de usuario dinámico (¡Vital para que María vea sus datos!)
          if (res.idUsuario) {
            localStorage.setItem('idUsuario', res.idUsuario.toString());
          }

          // 2. Normalizamos el rol (eliminamos espacios y pasamos a mayúsculas)
          const rolUsuario = (res.rol || '').toUpperCase().trim(); 
          console.log('Rol detectado y procesado:', rolUsuario);

          // 3. Lógica de redirección según el rol de la BD
          if (rolUsuario === 'ADMINISTRADOR') {
            this.router.navigate(['/panel-control-administrador']);
          } else if (rolUsuario === 'REPRESENTANTE') {
            console.log('Redirigiendo al panel de representante...');
            this.router.navigate(['/panel-representante']);
          } else {
            console.warn('Rol no reconocido:', rolUsuario);
            this.errorMessage = 'Acceso denegado: Rol no válido.';
            this.cdr.detectChanges();
          }
        },
        error: (err) => {
          console.error('Error capturado en el componente:', err);
          
          if (err.status === 401 || err.status === 403) {
            this.errorMessage = 'Credenciales incorrectas. Revisa tu email y contraseña.';
          } else if (err.status === 0) {
            this.errorMessage = 'No se pudo conectar con el servidor.';
          } else {
            this.errorMessage = 'Error inesperado al intentar iniciar sesión.';
          }
          
          // Forzamos a Angular a mostrar el mensaje de error
          this.cdr.detectChanges();
        }
      });
    } else {
      this.errorMessage = 'Por favor, rellena todos los campos.';
      this.cdr.detectChanges();
    }
  }

  /**
   * Genera la animación visual del fondo
   */
  generateConfetti() {
    for (let i = 0; i < 35; i++) {
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