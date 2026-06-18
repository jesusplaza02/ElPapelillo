// panel-control-administrador-auditoria.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PanelControlAdministradorAuditoriaService {
  private apiUrl = 'http://localhost:8080/api/auditoria'; 

  constructor(private http: HttpClient) {}

  // Ahora acepta parámetros opcionales
  getLogs(idUsuario?: number): Observable<any[]> {
    if (idUsuario) {
      const params = new HttpParams().set('idUsuario', idUsuario.toString());
      return this.http.get<any[]>(`${this.apiUrl}/usuario`, { params });
    }
    return this.http.get<any[]>(this.apiUrl);
  }

  getLogsFiltradosPorUsuario(idUsuarioActual: number): Observable<any[]> {
    return this.http.get<any[]>(`http://localhost:8080/api/auditoria?idUsuarioActual=${idUsuarioActual}`);
  }
}