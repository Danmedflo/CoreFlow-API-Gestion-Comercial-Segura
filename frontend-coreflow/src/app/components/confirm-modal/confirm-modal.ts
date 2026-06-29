import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-confirm-modal',
  imports: [CommonModule],
  templateUrl: './confirm-modal.html',
  styleUrl: './confirm-modal.css',
})
export class ConfirmModal {
  @Input() visible = false;
  @Input() titulo = 'Confirmar acción';
  @Input() mensaje = '';
  @Input() detalle = '';
  @Input() textoConfirmar = 'Confirmar';
  @Input() textoCancelar = 'Cancelar';

  @Output() confirmar = new EventEmitter<void>();
  @Output() cancelar = new EventEmitter<void>();

  confirmarAccion(): void {
    this.confirmar.emit();
  }

  cancelarAccion(): void {
    this.cancelar.emit();
  }
}