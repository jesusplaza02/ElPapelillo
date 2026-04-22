import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
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
    private cdr: ChangeDetectorRef 
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

  this.http.put(url, this.usuario)
    .subscribe({
      next: (response) => {
        this.mensajeExito = true;
        
        // ACTUALIZACIÓN SEGURA: 
        // Solo cambiamos el nombre, mantenemos el token y el resto de datos
        localStorage.setItem('nombreUsuario', this.usuario.nombre);
        
        console.log('Actualizado con éxito. Redirigiendo a Home...');
        
        // Redirige a /home. Asegúrate de que esta ruta existe en tu app-routing.ts
        setTimeout(() => {
          this.router.navigate(['/panel-representante']); 
        }, 2000);
      },
      error: (err) => {
        console.error('Error al actualizar:', err);
        // Si aquí recibes un 401 o 403, es cuando te echa al Login
      }
    });
}

  // --- EL MÉTODO CANCELAR QUE TE FALTABA ---
  cancelar() {
    // Te devuelve a la página anterior (Home o el listado)
    this.router.navigate(['/panel-representante']);
    // O si prefieres volver atrás exactamente a la última página:
    // window.history.back();
  }
}