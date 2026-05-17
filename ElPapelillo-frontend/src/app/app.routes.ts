import { Routes } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './componentes/login/auth.service';

import { LoginComponent } from './componentes/login/login'; 
import { RegistroComponent } from './componentes/registro/registro';
import { GestionAgrupacionesRepComponent } from './componentes/gestion-agrupaciones-rep/gestion-agrupaciones-rep';
import { PanelControlAdministradorComponent } from './componentes/panel-control-administrador/panel-control-administrador';
import { DocumentacionRepComponent } from './componentes/documentacion-rep/documentacion-rep';

import { EditarUsuarioComponent } from './componentes/editar-usuario/editar-usuario'; 
import { PoliticaPrivacidadComponent } from './componentes/politica-privacidad/politica-privacidad';

import { DetalleConcursoComponent } from './componentes/pagina-concurso/pagina-concurso';
import { DetalleAgrupacionComponent } from './componentes/pagina-agrupacion/pagina-agrupacion';
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
      router.navigate([userRole === 'ADMINISTRADOR' || userRole === 'SUPERADMIN' || userRole === 'SYSADMIN' ? '/panel-control-administrador' : '/panel-representante']);
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
  { path: 'politica-privacidad', component: PoliticaPrivacidadComponent },
  
  // RUTA DE EDICIÓN DE USUARIO (Para ambos roles, o ajusta según prefieras)
  { 
    path: 'editar-usuario', 
    component: EditarUsuarioComponent,
    canActivate: [roleGuard(['REPRESENTANTE', 'ADMINISTRADOR', 'SUPERADMIN', 'SYSADMIN'])] 
  },

  { 
    path: 'editar-usuario/:id', // Agregamos esta otra vez para que no te eche al login
    component: EditarUsuarioComponent,
    canActivate: [roleGuard(['REPRESENTANTE', 'ADMINISTRADOR', 'SUPERADMIN', 'SYSADMIN'])] 
  },

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

  // SOLO PARA ADMINISTRADORES
  { 
    path: 'panel-control-administrador', 
    component: PanelControlAdministradorComponent,
    canActivate: [roleGuard(['ADMINISTRADOR', 'SUPERADMIN','SYSADMIN'])] 
  },
  
  { 
    path: 'detalle-concurso/:id', 
    component: DetalleConcursoComponent,
    canActivate: [roleGuard(['ADMINISTRADOR', 'SUPERADMIN', 'SYSADMIN'])] 
  },

  { path: 'detalle-agrupacion/:id', 
    component: DetalleAgrupacionComponent,
    canActivate: [roleGuard(['ADMINISTRADOR', 'SUPERADMIN', 'SYSADMIN'])] 
  },

  { path: '**', redirectTo: 'login' }
];