import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Agrupacion } from './gestion-agrupaciones-rep.model';

@Injectable({ providedIn: 'root' })
export class AgrupacionService {
  // La URL de tu Controller de Java
  private API_URL = 'http://localhost:8080/api/agrupaciones'; 

  constructor(private http: HttpClient) {}

  getAgrupacionesPorRepresentante(idRep: number): Observable<Agrupacion[]> {
    return this.http.get<Agrupacion[]>(`${this.API_URL}/representante/${idRep}`);
  }
}