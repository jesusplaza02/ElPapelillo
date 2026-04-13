import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms'; 
import { HttpClientModule, HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [RouterLink, FormsModule, HttpClientModule],
  templateUrl: './registro.html',
  styleUrl: './registro.css'
})
export class RegistroComponent {
  
  // Solo los datos que el usuario rellena
  datosUsuario = {
    nombre: '',
    apellidos: '',
    dni: '',
    telefono: '',
    email: ''
  };

  constructor(private http: HttpClient) {}

  finalizarRegistro() {
    console.log('Solicitando registro y generación de contraseña...');

    // Seguimos sin usar TOKEN aquí, porque el usuario aún no existe
    this.http.post('http://tu-api.com/registro', this.datosUsuario)
      .subscribe({
        next: (res) => {
          alert('¡Registro iniciado! Revisa tu email para recibir tu contraseña temporal.');
          // Aquí solemos redirigir al login para que el usuario use su nueva clave
        },
        error: (err) => {
          console.error('Error:', err);
          alert('Hubo un error al procesar el registro.');
        }
      });
  }
}