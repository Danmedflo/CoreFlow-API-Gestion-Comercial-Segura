import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface PedidoModel {
  id?: number;
  cliente: string;
  fecha: string;
  total: number;
  estado: string;

  productoId?: number;
  productoNombre?: string;
  cantidad?: number;
  precioUnitario?: number;
}

export interface ComprarPedidoRequest {
  cliente: string;
  productoId: number;
  cantidad: number;
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

  obtenerPorId(id: number): Observable<PedidoModel> {
    return this.http.get<PedidoModel>(`${this.apiUrl}/${id}`);
  }

  crear(pedido: PedidoModel): Observable<PedidoModel> {
    return this.http.post<PedidoModel>(this.apiUrl, pedido);
  }

  comprar(request: ComprarPedidoRequest): Observable<PedidoModel> {
    return this.http.post<PedidoModel>(`${this.apiUrl}/comprar`, request);
  }

  actualizar(id: number, pedido: PedidoModel): Observable<PedidoModel> {
    return this.http.put<PedidoModel>(`${this.apiUrl}/${id}`, pedido);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarPorCliente(cliente: string): Observable<PedidoModel[]> {
    return this.http.get<PedidoModel[]>(`${this.apiUrl}/buscar?cliente=${cliente}`);
  }

  buscarPorEstado(estado: string): Observable<PedidoModel[]> {
    return this.http.get<PedidoModel[]>(`${this.apiUrl}/estado/${estado}`);
  }
}