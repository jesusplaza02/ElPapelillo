import { Routes } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './componentes/login/auth.service';

import { LoginComponent } from './componentes/login/login'; 
import { RegistroComponent } from './componentes/registro/registro';
import { GestionAgrupacionesRepComponent } from './componentes/gestion-agrupaciones-rep/gestion-agrupaciones-rep';
import { PanelControlAdministradorComponent } from './componentes/panel-control-administrador/panel-control-administrador';
import { DocumentacionRepComponent } from './componentes/documentacion-rep/documentacion-rep';

// FUNCIÓN GUARD CON CONTROL DE ROLES
const roleGuard = (allowedRoles: string[]) => {
  return () => {
    const router = inject(Router);
    const authService = inject(AuthService);
    
    const userRole = authService.getRol(); 
    if (authService.isLogged() && allowedRoles.includes(userRole)) {
      return true; 
    }

    if (authService.isLogged()) {
      alert('No tienes permisos para acceder aquí');
      router.navigate([userRole === 'ADMINISTRADOR' ? '/panel-control-administrador' : '/panel-representante']);
      return false;
    }

    router.navigate(['/login']);
    return false;
  };
};

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegistroComponent },
  
  // SOLO PARA REPRESENTANTES
  { 
    path: 'panel-representante', 
    component: GestionAgrupacionesRepComponent,
    canActivate: [roleGuard(['REPRESENTANTE'])] 
  },

  { 
    path: 'documentacion-agrupacion-rep/:id', 
    component: DocumentacionRepComponent,
    canActivate: [roleGuard(['REPRESENTANTE'])]
  },

  // SOLO PARA ADMINISTRADORES (Aquí es donde el Representante rebotaría)
  { 
    path: 'panel-control-administrador', 
    component: PanelControlAdministradorComponent,
    canActivate: [roleGuard(['ADMIN'])] 
  },

  { path: '**', redirectTo: 'login' }
];