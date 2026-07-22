import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { PedidoModel } from './pedido';

export interface PagoModel {
  id?: number;
  pedidoId: number;
  cliente: string;
  metodoPago: string;
  montoPagado: number;
  estadoPago: string;
  fechaPago: string;
}

export interface ComprobanteModel {
  id?: number;
  pedidoId: number;
  pagoId: number;
  tipoComprobante: string;
  numeroComprobante: string;
  fechaEmision: string;
  cliente: string;
  documentoCliente: string;
  razonSocial: string;
  ruc: string;
  direccionFiscal: string;
  subtotal: number;
  igv: number;
  total: number;
}

export interface ConfirmarPagoRequest {
  metodoPago: string;
  tipoComprobante: string;
  documentoCliente?: string;
  razonSocial?: string;
  ruc?: string;
  direccionFiscal?: string;
}

export interface PagoResponse {
  mensaje: string;
  pago: PagoModel;
  comprobante: ComprobanteModel;
}

@Injectable({
  providedIn: 'root'
})
export class Pago {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/pagos';

  listar(): Observable<PagoModel[]> {
    return this.http.get<PagoModel[]>(this.apiUrl);
  }

  listarMisPagos(): Observable<PagoModel[]> {
    return this.http.get<PagoModel[]>(`${this.apiUrl}/mis-pagos`);
  }

  obtenerPorPedido(pedidoId: number): Observable<PagoModel> {
    return this.http.get<PagoModel>(`${this.apiUrl}/pedido/${pedidoId}`);
  }

  confirmarPago(pedidoId: number, request: ConfirmarPagoRequest): Observable<PagoResponse> {
    return this.http.post<PagoResponse>(`${this.apiUrl}/confirmar/${pedidoId}`, request);
  }
}