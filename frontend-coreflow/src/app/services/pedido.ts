import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PedidoDetalleModel {
  id?: number;
  productoId: number;
  productoNombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface PedidoModel {
  id?: number;
  cliente: string;
  fecha: string;
  total: number;
  estado: string;
  detalles?: PedidoDetalleModel[];

  productoId?: number;
  productoNombre?: string;
  cantidad?: number;
  precioUnitario?: number;
}

export interface ItemCarritoRequest {
  productoId: number;
  cantidad: number;
}

export interface CheckoutPedidoRequest {
  items: ItemCarritoRequest[];
}

export interface PedidoPanelResumen {
  totalPedidos: number;
  pedidosPendientes: number;
  pedidosEnProceso: number;
  pedidosEntregados: number;
  pedidosCancelados: number;
  montoTotalVendido: number;
  montoPendiente: number;
}

export interface ActualizarEstadoPedidoRequest {
  estado: string;
}

export interface ActualizarPedidoOperativoRequest {
  items: ItemCarritoRequest[];
}

export interface PedidoOperacionResponse {
  mensaje: string;
  pedido: PedidoModel;
}

@Injectable({
  providedIn: 'root'
})
export class Pedido {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/pedidos';

  listar(): Observable<PedidoModel[]> {
    return this.http.get<PedidoModel[]>(this.apiUrl);
  }

  listarMisPedidos(): Observable<PedidoModel[]> {
    return this.http.get<PedidoModel[]>(`${this.apiUrl}/mis-pedidos`);
  }

  obtenerPorId(id: number): Observable<PedidoModel> {
    return this.http.get<PedidoModel>(`${this.apiUrl}/${id}`);
  }

  crear(pedido: PedidoModel): Observable<PedidoModel> {
    return this.http.post<PedidoModel>(this.apiUrl, pedido);
  }

  checkout(request: CheckoutPedidoRequest): Observable<PedidoModel> {
    return this.http.post<PedidoModel>(`${this.apiUrl}/checkout`, request);
  }

  actualizar(id: number, pedido: PedidoModel): Observable<PedidoModel> {
    return this.http.put<PedidoModel>(`${this.apiUrl}/${id}`, pedido);
  }

  actualizarEstado(
    id: number,
    request: ActualizarEstadoPedidoRequest
  ): Observable<PedidoOperacionResponse> {
    return this.http.put<PedidoOperacionResponse>(`${this.apiUrl}/${id}/estado`, request);
  }

  actualizarPedidoOperativo(
    id: number,
    request: ActualizarPedidoOperativoRequest
  ): Observable<PedidoOperacionResponse> {
    return this.http.put<PedidoOperacionResponse>(`${this.apiUrl}/${id}/operativo`, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarPorEstado(estado: string): Observable<PedidoModel[]> {
    return this.http.get<PedidoModel[]>(`${this.apiUrl}/estado/${encodeURIComponent(estado)}`);
  }

  buscarPorCliente(cliente: string): Observable<PedidoModel[]> {
    return this.http.get<PedidoModel[]>(`${this.apiUrl}/buscar?cliente=${encodeURIComponent(cliente)}`);
  }

  obtenerPanelResumen(): Observable<PedidoPanelResumen> {
    return this.http.get<PedidoPanelResumen>(`${this.apiUrl}/panel/resumen`);
  }
}