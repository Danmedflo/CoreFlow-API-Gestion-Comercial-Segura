import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ProductoModel } from './producto';

export interface CarritoItem {
  productoId: number;
  nombre: string;
  categoria: string;
  descripcion?: string;
  precio: number;
  stock: number;
  cantidad: number;
}

@Injectable({
  providedIn: 'root'
})
export class Carrito {
  private readonly storageKey = 'coreflow_carrito';
  private itemsSubject = new BehaviorSubject<CarritoItem[]>(this.leerCarrito());

  items$ = this.itemsSubject.asObservable();

  obtenerItems(): CarritoItem[] {
    return this.itemsSubject.value;
  }

  cantidadTotal(): number {
    return this.obtenerItems().reduce((total, item) => total + item.cantidad, 0);
  }

  total(): number {
    return this.obtenerItems().reduce((total, item) => {
      return total + item.precio * item.cantidad;
    }, 0);
  }

  agregarProducto(producto: ProductoModel, cantidad: number = 1): void {
    if (!producto.id) {
      throw new Error('El producto no tiene identificador.');
    }

    if (producto.stock <= 0) {
      throw new Error('El producto no tiene stock disponible.');
    }

    if (cantidad <= 0) {
      throw new Error('La cantidad debe ser mayor a cero.');
    }

    const items = [...this.obtenerItems()];
    const existente = items.find(item => item.productoId === producto.id);

    if (existente) {
      const nuevaCantidad = existente.cantidad + cantidad;

      if (nuevaCantidad > producto.stock) {
        throw new Error('La cantidad supera el stock disponible.');
      }

      existente.cantidad = nuevaCantidad;
      existente.stock = producto.stock;
      existente.precio = producto.precio;
    } else {
      if (cantidad > producto.stock) {
        throw new Error('La cantidad supera el stock disponible.');
      }

      items.push({
        productoId: producto.id,
        nombre: producto.nombre,
        categoria: producto.categoria,
        descripcion: producto.descripcion,
        precio: producto.precio,
        stock: producto.stock,
        cantidad
      });
    }

    this.guardarCarrito(items);
  }

  actualizarCantidad(productoId: number, cantidad: number): void {
    const items = [...this.obtenerItems()];
    const item = items.find(producto => producto.productoId === productoId);

    if (!item) {
      return;
    }

    if (cantidad <= 0) {
      this.eliminarProducto(productoId);
      return;
    }

    if (cantidad > item.stock) {
      throw new Error('La cantidad supera el stock disponible.');
    }

    item.cantidad = cantidad;
    this.guardarCarrito(items);
  }

  eliminarProducto(productoId: number): void {
    const items = this.obtenerItems().filter(item => item.productoId !== productoId);
    this.guardarCarrito(items);
  }

  vaciar(): void {
    this.guardarCarrito([]);
  }

  private leerCarrito(): CarritoItem[] {
    const data = localStorage.getItem(this.storageKey);

    if (!data) {
      return [];
    }

    try {
      return JSON.parse(data) as CarritoItem[];
    } catch {
      localStorage.removeItem(this.storageKey);
      return [];
    }
  }

  private guardarCarrito(items: CarritoItem[]): void {
    localStorage.setItem(this.storageKey, JSON.stringify(items));
    this.itemsSubject.next(items);
  }
}