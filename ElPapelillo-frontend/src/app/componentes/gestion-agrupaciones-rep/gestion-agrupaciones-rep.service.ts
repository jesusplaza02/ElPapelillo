import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Inscripcion, Agrupacion } from './gestion-agrupaciones-rep.model';

@Injectable({ providedIn: 'root' })
export class AgrupacionService {
  private INSCRIPCIONES_URL = 'http://localhost:8080/api/inscripciones'; 
  private AGRUPACIONES_URL = 'http://localhost:8080/api/agrupaciones'; 
  private CONCURSOS_URL = 'http://localhost:8080/api/concursos';

  constructor(private http: HttpClient) {}

  // Envía el token al backend
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('jwt_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}` 
    });
  }

  // 1. Obtener Inscripciones (la lista principal)
  getInscripcionesPorRepresentante(idRep: number): Observable<Inscripcion[]> {
    return this.http.get<Inscripcion[]>(`${this.INSCRIPCIONES_URL}/representante/${idRep}`, { headers: this.getHeaders() });
  }

  // 2. Obtener Agrupaciones base (para el modo EXISTENTE)
  getMisAgrupacionesBase(idRep: number): Observable<Agrupacion[]> {
    return this.http.get<Agrupacion[]>(`${this.AGRUPACIONES_URL}/representante/${idRep}`, { headers: this.getHeaders() });
  }

  // 3. Concursos Activos
  getConcursosActivos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.CONCURSOS_URL}/activos`, { headers: this.getHeaders() });
  }

  // 4. Crear la Inscripción
  crearInscripcion(payload: any): Observable<any> {
    return this.http.post(this.INSCRIPCIONES_URL, payload, { headers: this.getHeaders() });
  }
}