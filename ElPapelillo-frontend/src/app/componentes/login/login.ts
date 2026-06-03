import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router'; 
import { FormsModule } from '@angular/forms'; 
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service'; 
import { GlobalConfig } from '../../../constants';

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

  public config = GlobalConfig;

  errorMessage: string = '';
  showPassword: boolean = false;

  mostrarModalExitoGlobal: boolean = false;
  tituloModalExitoGlobal: string = '';
  contenidoModalExitoGlobal: string = '';

  mostrarModalErrorGlobal: boolean = false;
  tituloModalErrorGlobal: string = '';
  contenidoModalErrorGlobal: string = '';

  confettis: any[] = [];
  colors = ['#FFCDD2', '#F8BBD0', '#E1BEE7', '#D1C4E9', '#C5CAE9', '#B3E5FC', '#C8E6C9', '#FFF9C4'];

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef 
  ) {}

  ngOnInit() {
    localStorage.clear();
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  lanzarModalInformativo(titulo: string, contenido: string, tipo: 'success' | 'error'): void {
    if (tipo === 'success') {
      this.tituloModalExitoGlobal = titulo;
      this.contenidoModalExitoGlobal = contenido;
      this.mostrarModalExitoGlobal = true;
    } else {
      this.tituloModalErrorGlobal = titulo;
      this.contenidoModalErrorGlobal = contenido;
      this.mostrarModalErrorGlobal = true;
    }
    this.cdr.detectChanges();
  }

  onLogin() {
    this.errorMessage = '';
    
    if (!this.loginData.email || !this.loginData.password) {
      this.errorMessage = 'Por favor, rellena todos los campos.';
      this.cdr.detectChanges();
      return;
    }

    this.authService.login(this.loginData).subscribe({
      next: (res: any) => {
        console.log('Respuesta del servidor:', res);


        const rolUsuario = (res.rol || '').toUpperCase().trim(); 
        console.log('Rol detectado:', rolUsuario);

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
        if (err.status === 401) {
          this.errorMessage = err.error?.message || 'Email o contraseña incorrectos.';
        } else if (err.status === 0) {
          this.errorMessage = 'No se puede conectar con el servidor. Revisa si Spring Boot está activo.';
        } else {
          this.errorMessage = 'Ha ocurrido un error inesperado.';
        }
        this.cdr.detectChanges();
      }
    });
  }

  olvidarPassword() {
    const emailInput = document.querySelector('input[name="email"]') as HTMLInputElement;
    const email = emailInput?.value || this.loginData.email;

    if (!email || email.trim() === '') {
      this.lanzarModalInformativo(
        'Correo Requerido', 
        'Debes indicar un correo electrónico en el campo superior para recuperar la contraseña. Después te será enviado un correo con tus credenciales nuevas.', 
        'error'
      );
      return;
    }

    const url = 'http://localhost:8080/api/usuarios/recuperar-password';
    
    this.http.post(url, { email: email }).subscribe({
      next: () => {
        this.lanzarModalInformativo(
          'Contraseña Restablecida', 
          '¡Nueva contraseña generada y enviada! Por favor, revisa la bandeja de entrada de tu correo electrónico.', 
          'success'
        );
        this.errorMessage = '';
      },
      error: (err: any) => {
        this.lanzarModalInformativo(
          'Error de Envío', 
          'No se ha podido procesar la solicitud. Verifica que el email ingresado pertenezca a un usuario registrado.', 
          'error'
        );
      }
    });
  }

  
}