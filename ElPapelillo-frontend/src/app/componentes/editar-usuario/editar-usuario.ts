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
  // Objeto que almacenará los datos del usuario
  usuario: any = {
    nombreUsuario: '',
    email: '',
    telefono: '',
    direccion: '',
    telefonoEmergencia: '', // Solo para representantes
    cargo: '',              // Solo para administradores
    rol: ''
  };
  
  id: string | null = null;
  mensajeExito = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private cdr: ChangeDetectorRef // Necesario para forzar la actualización de la vista
  ) {}

  ngOnInit() {
    // Obtenemos el ID de la URL
    this.id = this.route.snapshot.paramMap.get('id');
    console.log('ID del usuario a editar:', this.id);

    if (this.id) {
      this.cargarUsuario();
    }
  }

  cargarUsuario() {
    this.http.get(`http://localhost:8080/api/usuarios/${this.id}`)
      .subscribe({
        next: (res: any) => {
          console.log('Datos de la BD:', res);
          this.usuario = res;

          // ESTO ES LO QUE FALTA: Mapear los nombres de la BD a tu objeto
          if (res.nombre_usuario) this.usuario.nombreUsuario = res.nombre_usuario;
          if (res.telefono_emergencia) this.usuario.telefonoEmergencia = res.telefono_emergencia;
          
          this.cdr.detectChanges(); 
        },
        error: (err) => console.error(err)
      });
  }

  actualizar() {
    if (!this.id) return;

    // Enviamos los datos actualizados al backend
    this.http.put(`http://localhost:8080/api/usuarios/${this.id}`, this.usuario)
      .subscribe({
        next: (response) => {
          this.mensajeExito = true;
          
          // Actualizamos el nombre en localStorage por si cambió (para el Header)
          localStorage.setItem('nombreUsuario', this.usuario.nombreUsuario);
          
          console.log('Usuario actualizado correctamente');
          
          // Redirigimos a la página principal tras 2 segundos
          setTimeout(() => {
            this.router.navigate(['/home']); 
          }, 2000);
        },
        error: (err) => {
          console.error('Error al actualizar el usuario:', err);
          alert('Hubo un error al guardar los cambios.');
        }
      });
  }

  cancelar() {
    // Volver a la pantalla anterior
    window.history.back();
  }
}