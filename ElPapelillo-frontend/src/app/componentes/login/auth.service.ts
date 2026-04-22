import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Método de Login mejorado
   * Usamos 'tap' para guardar los datos antes de que el componente los reciba
   */
  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      tap((res: any) => {
        // Guardamos el token y el rol en el almacenamiento local del navegador
        // El nombre de los campos (res.token, res.rol) debe coincidir con lo que envíe tu Spring
        if (res) {
          localStorage.setItem('token', res.token);
          localStorage.setItem('rol', res.rol);
          localStorage.setItem('usuario', res.username);
        }
      })
    );
  }

  registro(userData: any) {
    return this.http.post(`${this.apiUrl}/usuarios/registro`, userData);
  }

  // --- MÉTODOS PARA EL GUARD ---

  // Verifica si existe un token (Autenticación)
  isLogged(): boolean {
    return !!localStorage.getItem('token');
  }

  // Devuelve el rol guardado (Autorización)
  getRol(): string {
    return localStorage.getItem('rol') || '';
  }

  // Limpia la sesión al cerrar
  logout() {
    localStorage.clear();
  }
}