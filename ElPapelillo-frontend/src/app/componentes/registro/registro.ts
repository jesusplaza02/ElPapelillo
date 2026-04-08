import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './registro.html', // <--- Quítale el ".component" aquí
  styleUrl: './registro.css'      // <--- Quítale el ".component" aquí
})
export class RegistroComponent {}