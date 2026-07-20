import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProductoModel {
  id?: number;
  nombre: string;
  precio: number;
  stock: number;
  categoria: string;
  descripcion?: string;
}

export interface ProductoPanelResumen {
  totalProductos: number;
  stockTotal: number;
  productosStockCritico: number;
  productosSinStock: number;
  valorInventario: number;
}

@Injectable({
  providedIn: 'root'
})
export class Producto {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/productos';

  listar(): Observable<ProductoModel[]> {
    return this.http.get<ProductoModel[]>(this.apiUrl);
  }

  obtenerPorId(id: number): Observable<ProductoModel> {
    return this.http.get<ProductoModel>(`${this.apiUrl}/${id}`);
  }

  crear(producto: ProductoModel): Observable<ProductoModel> {
    return this.http.post<ProductoModel>(this.apiUrl, producto);
  }

  actualizar(id: number, producto: ProductoModel): Observable<ProductoModel> {
    return this.http.put<ProductoModel>(`${this.apiUrl}/${id}`, producto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarPorNombre(nombre: string): Observable<ProductoModel[]> {
    return this.http.get<ProductoModel[]>(`${this.apiUrl}/buscar?nombre=${encodeURIComponent(nombre)}`);
  }

  buscarPorCategoria(categoria: string): Observable<ProductoModel[]> {
    return this.http.get<ProductoModel[]>(`${this.apiUrl}/categoria/${encodeURIComponent(categoria)}`);
  }

  obtenerPanelResumen(): Observable<ProductoPanelResumen> {
    return this.http.get<ProductoPanelResumen>(`${this.apiUrl}/panel/resumen`);
  }

  listarStockCritico(limite: number = 5): Observable<ProductoModel[]> {
    return this.http.get<ProductoModel[]>(`${this.apiUrl}/panel/stock-critico?limite=${limite}`);
  }
}