import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
// He cambiado el nombre para que coincida con el componente
export class PanelControlAdministradorAuditoriaService {
  private apiUrl = 'http://localhost:8080/api/auditoria'; 

  constructor(private http: HttpClient) {}

  getLogs(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }
}