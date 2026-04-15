import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Agrupacion } from './gestion-agrupaciones-rep.model';

@Injectable({ providedIn: 'root' })
export class AgrupacionService {
  private API_URL = 'http://localhost:8080/api/agrupaciones'; 

  constructor(private http: HttpClient) {}

  // Listado principal (GET)
  getAgrupacionesPorRepresentante(idRep: number): Observable<Agrupacion[]> {
    return this.http.get<Agrupacion[]>(`${this.API_URL}/representante/${idRep}`);
  }

  // Concursos activos para el select (GET)
  getConcursosActivos(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8080/api/concursos/activos');
  }

  // Guardar nueva agrupación (POST)
  crearAgrupacion(agrupacion: any): Observable<any> {
    return this.http.post<any>(this.API_URL, agrupacion);
  }
}