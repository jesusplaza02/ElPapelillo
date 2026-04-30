import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PanelControlAdministradorConcursoService {
  
  private apiUrl = 'http://localhost:8080/api/concursos';

  constructor(private http: HttpClient) {}

  getMisConcursos(idUsuario: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/mis-concursos/${idUsuario}`);
  }

  // --- AÑADE ESTOS MÉTODOS PARA QUITAR EL ROJO ---
  
  crearConcurso(concurso: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, concurso);
  }

  actualizarConcurso(id: number, concurso: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, concurso);
  }

  eliminarConcurso(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}