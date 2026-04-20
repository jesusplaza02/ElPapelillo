import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { Usuario } from './panel-control-administrador-usuario.model';

@Injectable({
  providedIn: 'root'
})
export class PanelControlAdministradorUsuarioService {

  private apiUrl = 'http://localhost:8080/api/usuarios';

  constructor(private http: HttpClient) { }

  getUsuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.apiUrl).pipe(
      catchError(this.handleError)
    );
  }

  buscarUsuariosActivos(nombre: string): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.apiUrl}/buscar-activos/${nombre}`).pipe(
      catchError(this.handleError)
    );
  }

  crearUsuario(usuario: any): Observable<Usuario> {
    // Obtenemos el ID del administrador que está creando al usuario
    const idEjecutor = localStorage.getItem('idUsuario') || 1;
    
    // Lo enviamos como parámetro por si el backend también lo requiere en el POST
    const urlConParametro = `${this.apiUrl}?idEjecutor=${idEjecutor}`;

    return this.http.post<Usuario>(urlConParametro, usuario).pipe(
      catchError(this.handleError)
    );
  }

  actualizarUsuario(usuario: any): Observable<Usuario> {
    // Extraemos el ID del usuario que vamos a modificar
    let id = usuario.idUsuario || usuario.id;
    
    // Limpieza de seguridad por si llega con formato "1:1"
    if (typeof id === 'string' && id.includes(':')) {
      id = id.split(':')[0]; 
    }
    
    // Obtenemos el ID del administrador que está haciendo la acción
    // (Si no tienes 'idUsuario' guardado en localStorage, enviamos un 1 por defecto)
    const idEjecutor = localStorage.getItem('idUsuario') || 1;

    console.log(`Actualizando usuario ${id} ejecutado por admin ${idEjecutor}`);

    // ¡AQUÍ ESTÁ LA SOLUCIÓN! Añadimos el parámetro que exigía Spring Boot
    const urlConParametro = `${this.apiUrl}/${id}?idEjecutor=${idEjecutor}`;

    return this.http.put<Usuario>(urlConParametro, usuario).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    if (error.status === 0) {
      console.error('Error de red o CORS detectado:', error.error);
    } else {
      console.error(`Backend retornó el código ${error.status}, cuerpo: `, error.error);
    }
    return throwError(() => new Error('Algo salió mal; por favor, inténtelo de nuevo más tarde.'));
  }

  actualizarUsuarioConEjecutor(idUsuario: number, usuario: any, idEjecutor: number): Observable<any> {
  // Construimos la URL con el parámetro de consulta ?idEjecutor=X
  const url = `${this.apiUrl}/${idUsuario}?idEjecutor=${idEjecutor}`;
  
  return this.http.put(url, usuario);
}
}