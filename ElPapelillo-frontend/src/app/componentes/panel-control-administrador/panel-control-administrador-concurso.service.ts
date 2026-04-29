import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PanelControlAdministradorConcursoService {
  
  // 1. Cambiamos la URL a la de tu servidor local de Spring Boot
  private apiUrl = 'http://localhost:8080/api/concursos';

  constructor(private http: HttpClient) {}

  /**
   * Obtiene los concursos según el rol del usuario:
   * - Si es SYSADMIN, el backend devolverá todos.
   * - Si es ADMIN/SUPERADMIN, el backend devolverá solo los de su organización.
   */
  getMisConcursos(idUsuario: number): Observable<any[]> {
    // 2. Usamos el endpoint que creamos en el Controller del Java
    return this.http.get<any[]>(`${this.apiUrl}/mis-concursos/${idUsuario}`);
  }

  /**
   * Opcional: Obtener solo concursos activos
   */
  getConcursosActivos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/activos`);
  }
}