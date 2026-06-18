import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PanelControlAdministradorOrganizacionService {
  private apiUrl = 'http://localhost:8080/api/organizaciones';

  constructor(private http: HttpClient) { }

  getOrganizaciones(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  crearOrganizacion(org: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, org);
  }


  eliminarOrganizacion(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  actualizarOrganizacion(id: number, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, data);
  }
}