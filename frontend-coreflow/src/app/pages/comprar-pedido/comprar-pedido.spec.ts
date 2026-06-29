import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComprarPedido } from './comprar-pedido';

describe('ComprarPedido', () => {
  let component: ComprarPedido;
  let fixture: ComponentFixture<ComprarPedido>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ComprarPedido]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ComprarPedido);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
