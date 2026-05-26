import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      tap((res: any) => {
        if (res) {
          localStorage.setItem('token', res.token);
          
          const rolLimpio = (res.rol || '').toUpperCase().trim();
          localStorage.setItem('rol', rolLimpio);
          localStorage.setItem('rolUsuario', rolLimpio); 

          localStorage.setItem('usuario', res.username || res.email || '');
          localStorage.setItem('email', res.email || '');
          localStorage.setItem('nombreUsuario', res.nombre || '');
          localStorage.setItem('idUsuario', res.idUsuario?.toString() || '');

          const orgId = res.id_organizacion || res.idOrganizacion;
          if (orgId != null) {
            localStorage.setItem('id_organizacion', orgId.toString());
            console.log('ID Org guardado:', orgId);
          }
        }
      })
    );
  }

  isLogged(): boolean {
    return !!localStorage.getItem('token');
  }

  getRol(): string {
    // Intentamos pillar cualquiera de los dos
    return localStorage.getItem('rolUsuario') || localStorage.getItem('rol') || '';
  }

  logout() {
    localStorage.clear();
  }
}