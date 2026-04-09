import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router'; // Añadimos esto para que no fallen los botones
import { GestionAgrupacionesRepComponent } from './gestion-agrupaciones-rep';

describe('GestionAgrupacionesRepComponent', () => {
  // Aquí usamos el nombre completo de la clase
  let component: GestionAgrupacionesRepComponent;
  let fixture: ComponentFixture<GestionAgrupacionesRepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      // Importamos la clase correcta
      imports: [GestionAgrupacionesRepComponent],
      // Añadimos el proveedor de rutas porque tu HTML usa routerLink
      providers: [provideRouter([])] 
    }).compileComponents();

    fixture = TestBed.createComponent(GestionAgrupacionesRepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // Esto es mejor que await fixture.whenStable() para tests simples
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});