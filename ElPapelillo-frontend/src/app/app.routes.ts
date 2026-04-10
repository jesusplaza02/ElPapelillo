import { Routes } from '@angular/router';
// Importamos apuntando a tus archivos sin el ".component"
import { LoginComponent } from './componentes/login/login'; 
import { RegistroComponent } from './componentes/registro/registro';
import { GestionAgrupacionesRepComponent } from './componentes/gestion-agrupaciones-rep/gestion-agrupaciones-rep';
import { PanelControlAdministradorComponent } from './componentes/panel-control-administrador/panel-control-administrador';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegistroComponent },
  { path: 'panel-representante', component: GestionAgrupacionesRepComponent },
  { path: 'panel-control-administrador', component: PanelControlAdministradorComponent },
];