import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Pedido, PedidoModel } from '../../services/pedido';
import { Pago, ConfirmarPagoRequest } from '../../services/pago';

@Component({
  selector: 'app-pago-confirmacion',
  imports: [CommonModule, FormsModule],
  templateUrl: './pago-confirmacion.html',
  styleUrl: './pago-confirmacion.css',
})
export class PagoConfirmacionPage implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private pedidoService = inject(Pedido);
  private pagoService = inject(Pago);

  pedido: PedidoModel | null = null;

  metodoPago = 'YAPE';
  tipoComprobante = 'BOLETA';
  documentoCliente = '';

  razonSocial = '';
  ruc = '';
  direccionFiscal = '';

  cargando = false;
  pagando = false;
  mensaje = '';
  error = '';

  metodosPago = ['YAPE', 'PLIN', 'TRANSFERENCIA', 'TARJETA', 'EFECTIVO'];
  tiposComprobante = ['BOLETA', 'FACTURA'];

  ngOnInit(): void {
    this.cargarPedido();
  }

  cargarPedido(): void {
    const pedidoId = Number(this.route.snapshot.paramMap.get('pedidoId'));

    if (!pedidoId) {
      this.error = 'No se encontró el pedido seleccionado.';
      return;
    }

    this.cargando = true;
    this.error = '';
    this.mensaje = '';

    this.pedidoService.obtenerPorId(pedidoId).subscribe({
      next: (pedido) => {
        this.pedido = pedido;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar la información del pedido.';
        this.cargando = false;
      }
    });
  }

  confirmarPago(): void {
    if (!this.pedido?.id) {
      this.error = 'No se encontró el pedido.';
      return;
    }

    if (this.pedido.estado?.toUpperCase() === 'CANCELADO') {
      this.error = 'No se puede pagar un pedido cancelado.';
      return;
    }

    const validacion = this.validarFormulario();

    if (validacion) {
      this.error = validacion;
      return;
    }

    const request: ConfirmarPagoRequest = {
      metodoPago: this.metodoPago,
      tipoComprobante: this.tipoComprobante,
      documentoCliente: this.documentoCliente,
      razonSocial: this.razonSocial,
      ruc: this.ruc,
      direccionFiscal: this.direccionFiscal
    };

    this.pagando = true;
    this.error = '';
    this.mensaje = '';

    this.pagoService.confirmarPago(this.pedido.id, request).subscribe({
      next: (response) => {
        this.mensaje = response.mensaje || 'Pago confirmado correctamente.';
        this.pagando = false;

        setTimeout(() => {
          this.router.navigate(['/comprobantes/pedido', this.pedido?.id]);
        }, 700);
      },
      error: (err: any) => {
        this.error =
          err?.error?.mensaje ||
          'No se pudo confirmar el pago. Verifica los datos ingresados.';
        this.pagando = false;
      }
    });
  }

  validarFormulario(): string {
    if (!this.metodoPago) {
      return 'Selecciona un método de pago.';
    }

    if (!this.tipoComprobante) {
      return 'Selecciona el tipo de comprobante.';
    }

    if (this.tipoComprobante === 'FACTURA') {
      if (!this.razonSocial.trim()) {
        return 'La razón social es obligatoria para factura.';
      }

      if (!this.ruc.trim()) {
        return 'El RUC es obligatorio para factura.';
      }

      if (this.ruc.trim().length !== 11) {
        return 'El RUC debe tener 11 dígitos.';
      }
    }

    return '';
  }

  volver(): void {
    this.router.navigate(['/pedidos']);
  }

  totalPedido(): number {
    return Number(this.pedido?.total || 0);
  }

  subtotal(): number {
    return Math.round((this.totalPedido() / 1.18) * 100) / 100;
  }

  igv(): number {
    return Math.round((this.totalPedido() - this.subtotal()) * 100) / 100;
  }

  detallesPedido() {
    return this.pedido?.detalles || [];
  }

  esFactura(): boolean {
    return this.tipoComprobante === 'FACTURA';
  }

  estadoFormateado(): string {
    return this.pedido?.estado?.replace('_', ' ') || 'PENDIENTE';
  }
}