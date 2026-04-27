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

  // Configuración visual (Confeti)
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
    
    // Validación básica antes de enviar
    if (!this.loginData.email || !this.loginData.password) {
      this.errorMessage = 'Por favor, rellena todos los campos.';
      this.cdr.detectChanges();
      return;
    }

    this.authService.login(this.loginData).subscribe({
      next: (res: any) => {
        console.log('Respuesta del servidor:', res);

        // 1. GUARDADO DE DATOS EN LOCALSTORAGE (Para el Header y perfil)
        // Guardamos el ID del usuario
        if (res.idUsuario) {
          localStorage.setItem('idUsuario', res.idUsuario.toString());
        }

        // Guardamos el ID de la organización para los filtros de visibilidad
        if (res.id_organizacion !== undefined && res.id_organizacion !== null) {
          localStorage.setItem('id_organizacion', res.id_organizacion.toString());
        } else if (res.idOrganizacion) { 
          // Por si tu Java usa camelCase en el JSON
          localStorage.setItem('id_organizacion', res.idOrganizacion.toString());
        }
        // -----------------------
                
        // Guardamos el Email (Para que el Header lo muestre)
        if (res.email) {
          localStorage.setItem('email', res.email);
        }

        // Guardamos el Nombre (Opcional, por si quieres mostrar "Hola, Juan")
        if (res.nombre) {
          localStorage.setItem('nombreUsuario', res.nombre);
        }

        if (res.rol) {
          localStorage.setItem('rolUsuario', res.rol.toString().toUpperCase().trim());
        }

        // 2. PROCESADO DEL ROL
        const rolUsuario = (res.rol || '').toUpperCase().trim(); 
        console.log('Rol detectado:', rolUsuario);

        // 3. REDIRECCIÓN SEGÚN ROL
        if (rolUsuario === 'ADMINISTRADOR'|| rolUsuario === 'SUPERADMIN' || rolUsuario === 'SYSADMIN') {
          this.router.navigate(['/panel-control-administrador']);
        } else if (rolUsuario === 'REPRESENTANTE') {
          this.router.navigate(['/panel-representante']);
        } else {
          console.warn('Rol no reconocido:', rolUsuario);
          this.errorMessage = 'Acceso denegado: Rol no válido.';
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error('Error capturado en el login:', err);
        
        // Manejo de errores dinámico según la respuesta del LoginController
        if (err.status === 401) {
          // Capturamos el mensaje que enviamos con Map.of desde Java
          this.errorMessage = err.error?.message || 'Email o contraseña incorrectos.';
        } else if (err.status === 0) {
          this.errorMessage = 'No se puede conectar con el servidor. Revisa si Spring Boot está activo.';
        } else {
          this.errorMessage = 'Ha ocurrido un error inesperado.';
        }
        
        // FORZAMOS el refresco para que el mensaje aparezca sin clics adicionales
        this.cdr.detectChanges();
      }
    });
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