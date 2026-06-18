import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PanelControlAdministrador } from './panel-control-administrador';

describe('PanelControlAdministrador', () => {
  let component: PanelControlAdministrador;
  let fixture: ComponentFixture<PanelControlAdministrador>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PanelControlAdministrador],
    }).compileComponents();

    fixture = TestBed.createComponent(PanelControlAdministrador);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
