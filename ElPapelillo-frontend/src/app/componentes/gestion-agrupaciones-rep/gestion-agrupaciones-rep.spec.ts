import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router'; 
import { GestionAgrupacionesRepComponent } from './gestion-agrupaciones-rep';

describe('GestionAgrupacionesRepComponent', () => {
  // Aquí usamos el nombre completo de la clase
  let component: GestionAgrupacionesRepComponent;
  let fixture: ComponentFixture<GestionAgrupacionesRepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionAgrupacionesRepComponent],
      providers: [provideRouter([])] 
    }).compileComponents();

    fixture = TestBed.createComponent(GestionAgrupacionesRepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); 
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});