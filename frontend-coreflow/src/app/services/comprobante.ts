import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { PedidoModel } from './pedido';
import { ComprobanteModel } from './pago';

export interface ComprobanteResponse {
  comprobante: ComprobanteModel;
  pedido: PedidoModel;
}

@Injectable({
  providedIn: 'root'
})
export class Comprobante {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/comprobantes';

  listar(): Observable<ComprobanteModel[]> {
    return this.http.get<ComprobanteModel[]>(this.apiUrl);
  }

  listarMisComprobantes(): Observable<ComprobanteModel[]> {
    return this.http.get<ComprobanteModel[]>(`${this.apiUrl}/mis-comprobantes`);
  }

  obtenerPorId(id: number): Observable<ComprobanteResponse> {
    return this.http.get<ComprobanteResponse>(`${this.apiUrl}/${id}`);
  }

  obtenerPorPedido(pedidoId: number): Observable<ComprobanteResponse> {
    return this.http.get<ComprobanteResponse>(`${this.apiUrl}/pedido/${pedidoId}`);
  }
}