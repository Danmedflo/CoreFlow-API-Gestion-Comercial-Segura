import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { Comprobante, ComprobanteResponse } from '../../services/comprobante';

@Component({
  selector: 'app-comprobante',
  imports: [CommonModule],
  templateUrl: './comprobante.html',
  styleUrl: './comprobante.css',
})
export class ComprobantePage implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private comprobanteService = inject(Comprobante);

  data: ComprobanteResponse | null = null;

  cargando = false;
  error = '';

  ngOnInit(): void {
    this.cargarComprobante();
  }

  cargarComprobante(): void {
    const pedidoId = Number(this.route.snapshot.paramMap.get('pedidoId'));
    const comprobanteId = Number(this.route.snapshot.paramMap.get('id'));

    this.cargando = true;
    this.error = '';

    if (pedidoId) {
      this.comprobanteService.obtenerPorPedido(pedidoId).subscribe({
        next: (response: ComprobanteResponse) => {
          this.data = response;
          this.cargando = false;
        },
        error: (err: any) => {
          this.error =
            err?.error?.mensaje ||
            'No se pudo cargar el comprobante del pedido.';
          this.cargando = false;
        }
      });

      return;
    }

    if (comprobanteId) {
      this.comprobanteService.obtenerPorId(comprobanteId).subscribe({
        next: (response: ComprobanteResponse) => {
          this.data = response;
          this.cargando = false;
        },
        error: (err: any) => {
          this.error =
            err?.error?.mensaje ||
            'No se pudo cargar el comprobante solicitado.';
          this.cargando = false;
        }
      });

      return;
    }

    this.error = 'No se encontró el comprobante seleccionado.';
    this.cargando = false;
  }

  volver(): void {
    this.router.navigate(['/pedidos']);
  }

  imprimir(): void {
    window.print();
  }

  detallesPedido() {
    return this.data?.pedido?.detalles || [];
  }

  tipoComprobante(): string {
    return this.data?.comprobante?.tipoComprobante || 'COMPROBANTE';
  }

  esFactura(): boolean {
    return this.tipoComprobante().toUpperCase() === 'FACTURA';
  }

  fechaFormateada(): string {
    return this.data?.comprobante?.fechaEmision || '';
  }
}