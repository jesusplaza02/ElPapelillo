import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PanelControlAdministradorConcursoService {
  private apiUrl = 'http://tu-api.com/concursos';

  constructor(private http: HttpClient) {}

  getConcursos(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }
}