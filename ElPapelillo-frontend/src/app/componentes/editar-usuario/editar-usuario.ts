import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-editar-usuario',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './editar-usuario.html',
  styleUrl: './editar-usuario.css'
})
export class EditarUsuarioComponent implements OnInit {
  usuario: any = {
    idUsuario: null,
    nombre: '',
    email: '',
    telefono: '',
    direccion: '',
    contacto_emergencia: '', 
    cargo: '',              
    rol: '',
    type: '' 
  };
  
  idUrl: any = null; 
  mensajeExito = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private location: Location
  ) {}

  ngOnInit() {
    console.log('%c --- DEBUG SESIÓN --- ', 'background: #222; color: #bada55');
    
    // 1. OBTENER NUESTRA IDENTIDAD REAL (A prueba de errores de JSON)
    const dataStored = localStorage.getItem('usuario');
    let miIdReal: any = null;

    if (dataStored) {
      try {
        // Solo intentamos parsear si parece un objeto JSON (empieza por {)
        if (dataStored.trim().startsWith('{')) {
          const sesion = JSON.parse(dataStored);
          miIdReal = sesion.idUsuario || sesion.id;
        } else {
          // Si no es JSON (es un texto como el email), buscamos el ID en otra clave
          console.warn('El storage "usuario" es texto plano, no JSON.');
          miIdReal = localStorage.getItem('idUsuario'); 
        }
      } catch (e) {
        miIdReal = localStorage.getItem('idUsuario');
      }
    }

    // 2. Si todo falla, forzamos el ID 1 (Tú) para que no se rompa nada en las pruebas
    if (!miIdReal) miIdReal = 1; 

    this.idUrl = this.route.snapshot.paramMap.get('id');
    
    console.log('ID en la URL:', this.idUrl);
    console.log('ID real detectado (Fijo):', miIdReal);

    // SEGURIDAD: Siempre cargamos NUESTRO perfil, ignoramos la URL para evitar confusiones
    this.cargarUsuario(miIdReal);
  }

  cargarUsuario(id: any) {
    this.http.get(`http://localhost:8080/api/usuarios/${id}`)
      .subscribe({
        next: (res: any) => {
          this.usuario = res;
          // Mapeo por si el campo en DB es diferente
          if (res.telefono_emergencia) this.usuario.contacto_emergencia = res.telefono_emergencia;
          this.cdr.detectChanges(); 
        },
        error: (err) => console.error('Error al cargar:', err)
      });
  }

  actualizar() {
    // Recuperar identidad para el guardado
    const dataStored = localStorage.getItem('usuario');
    let miIdReal: any = 1; // Default por seguridad

    try {
      if (dataStored && dataStored.trim().startsWith('{')) {
        const sesion = JSON.parse(dataStored);
        miIdReal = sesion.idUsuario || sesion.id;
      } else {
        miIdReal = localStorage.getItem('idUsuario') || 1;
      }
    } catch(e) { miIdReal = 1; }

    // BLINDAJE: El ID que se envía al servidor es el nuestro, SIEMPRE.
    this.usuario.idUsuario = miIdReal;

    const url = `http://localhost:8080/api/usuarios/perfil?idEjecutor=${miIdReal}`;

    this.http.put(url, this.usuario).subscribe({
      next: (response) => {
        this.mensajeExito = true;
        localStorage.setItem('nombreUsuario', this.usuario.nombre);
        
        setTimeout(() => {
          const r = this.usuario.rol;
          this.router.navigate([r === 'REPRESENTANTE' ? '/panel-representante' : '/panel-control-administrador']);
        }, 2000);
      },
      error: (err) => alert('Error al guardar datos: ' + err.message)
    });
  }

  cancelar() { this.location.back(); }
}