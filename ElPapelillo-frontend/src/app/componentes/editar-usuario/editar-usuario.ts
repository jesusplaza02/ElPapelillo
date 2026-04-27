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
    idUsuario: null,    // Importante: Coincide con @Column(name = "idUsuario")
    nombre: '',
    email: '',
    telefono: '',
    direccion: '',
    contacto_emergencia: '', 
    cargo: '',              
    rol: '',
    type: ''            // Necesario por @JsonTypeInfo de tu Java
  };
  
  id: string | null = null;
  mensajeExito = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private location: Location
  ) {}

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id');
    if (this.id) {
      this.cargarUsuario();
    }
  }

  cargarUsuario() {
    this.http.get(`http://localhost:8080/api/usuarios/${this.id}`)
      .subscribe({
        next: (res: any) => {
          console.log('Datos cargados de Java:', res);
          this.usuario = res;

          // Mapeo por si el JSON de Representante trae 'telefono_emergencia'
          if (res.telefono_emergencia) {
            this.usuario.contacto_emergencia = res.telefono_emergencia;
          }

          this.cdr.detectChanges(); 
        },
        error: (err) => console.error('Error al cargar:', err)
      });
  }

  actualizar() {
  if (!this.id) return;

  const idEjecutor = this.id; 
  const url = `http://localhost:8080/api/usuarios/${this.id}?idEjecutor=${idEjecutor}`;

  this.http.put(url, this.usuario).subscribe({
    next: (response) => {
      this.mensajeExito = true;
      
      // Actualizamos el nombre en el storage para que el header se vea bien
      localStorage.setItem('nombreUsuario', this.usuario.nombre);
      
      console.log('Actualizado con éxito. Redirigiendo...');
      
      setTimeout(() => {
        // CORRECCIÓN: Redirigir según el rol del usuario que acaba de editar
        const rol = this.usuario.rol; // O usa tu authService.getRol()

        if (rol === 'REPRESENTANTE') {
          this.router.navigate(['/panel-representante']);
        } else {
          // Si es ADMIN, SUPERADMIN o SYSADMIN, vuelve al panel de control
          this.router.navigate(['/panel-control-administrador']);
        }
      }, 2000);
    },
    error: (err) => {
      console.error('Error al actualizar:', err);
    }
  });
}

  cancelar() {
  this.location.back()
  }
}
