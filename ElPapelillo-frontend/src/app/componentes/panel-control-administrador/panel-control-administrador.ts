import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-panel-control-administrador',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './panel-control-administrador.html',
  styleUrl: './panel-control-administrador.css'
})
export class PanelControlAdministradorComponent {
  isMenuOpen = false;

  // Datos de ejemplo para la tabla de usuarios
  usuarios = [
    { nombre: 'Carlos Rodríguez', email: 'carlos@ejemplo.com', rol: 'Representante' },
    { nombre: 'María García', email: 'maria@ejemplo.com', rol: 'Administrador' }
  ];

  // Datos de ejemplo para la auditoría
 logs = [
  { tipo: 'auth-event', icon: 'person_add', msg: 'Creación de nuevo usuario: admin_pepe@uma.es', fecha: '10/04/2026', time: '10:20', cat: 'Usuarios' },
  { tipo: 'error', icon: 'person_remove', msg: 'Borrado de usuario: rep_antonio@gmail.com', fecha: '10/04/2026', time: '10:45', cat: 'Usuarios' },
  { tipo: 'info', icon: 'manage_accounts', msg: 'Actualización de datos: perfil_carlos_2026', fecha: '09/04/2026', time: '11:00', cat: 'Usuarios' },
  { tipo: 'success', icon: 'trophy', msg: 'Creación de concurso: Carnaval de Málaga 2026', fecha: '09/04/2026', time: '11:15', cat: 'Concursos' },
  { tipo: 'error', icon: 'event_busy', msg: 'Borrado de concurso: Evento Prueba Verano', fecha: '08/04/2026', time: '11:30', cat: 'Concursos' },
  { tipo: 'success', icon: 'how_to_reg', msg: 'Aprobación inscripción: Agrupación "Los Malagueños"', fecha: '08/04/2026', time: '12:00', cat: 'Inscripciones' },
  { tipo: 'error', icon: 'person_cancel', msg: 'Rechazo inscripción: Agrupación "Coro del Puerto"', fecha: '07/04/2026', time: '12:15', cat: 'Inscripciones' },
  { tipo: 'success', icon: 'task_alt', msg: 'Aprobado documento: DNI_Representante.pdf', fecha: '07/04/2026', time: '12:30', cat: 'Documentación' },
  { tipo: 'error', icon: 'block', msg: 'Rechazo documento: Seguro_Responsabilidad.pdf', fecha: '06/04/2026', time: '12:45', cat: 'Documentación' },
  { tipo: 'info', icon: 'cloud_upload', msg: 'Subida documento fianza: Fianza_2026_LosMalagueños.pdf', fecha: '06/04/2026', time: '13:00', cat: 'Fianzas' },
  { tipo: 'info', icon: 'picture_as_pdf', msg: 'Descarga listados PDF: Clasificación preliminar', fecha: '05/04/2026', time: '13:15', cat: 'Reportes' },
  { tipo: 'error', icon: 'mail_lock', msg: 'Fallo del sistema: envío correo credenciales a nuevo_rep@uma.es', fecha: '05/04/2026', time: '13:30', cat: 'Sistema' }
];

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }

  // Cerrar menú si se hace click fuera (reemplaza al window.onclick de tu HTML)
  @HostListener('document:click', ['$event'])
  clickout(event: any) {
    if (!event.target.closest('.user-menu-container')) {
      this.isMenuOpen = false;
    }
  }
}