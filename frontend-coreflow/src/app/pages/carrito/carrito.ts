import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { Carrito, CarritoItem } from '../../services/carrito';
import { Pedido } from '../../services/pedido';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-carrito',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './carrito.html',
  styleUrl: './carrito.css',
})
export class CarritoPage implements OnInit {
  private carritoService = inject(Carrito);
  private pedidoService = inject(Pedido);
  private auth = inject(Auth);
  private router = inject(Router);

  items: CarritoItem[] = [];
  procesando = false;
  mensaje = '';
  error = '';

  ngOnInit(): void {
    this.cargarCarrito();
  }

  cargarCarrito(): void {
    this.items = this.carritoService.obtenerItems();
  }

  actualizarCantidad(item: CarritoItem, cantidad: number): void {
    this.error = '';
    this.mensaje = '';

    try {
      this.carritoService.actualizarCantidad(item.productoId, Number(cantidad));
      this.cargarCarrito();
    } catch (error: any) {
      this.error = error.message || 'No se pudo actualizar la cantidad.';
    }
  }

  eliminarItem(item: CarritoItem): void {
    this.carritoService.eliminarProducto(item.productoId);
    this.cargarCarrito();
    this.mensaje = `Producto "${item.nombre}" retirado del carrito.`;
    this.error = '';
  }

  vaciarCarrito(): void {
    this.carritoService.vaciar();
    this.cargarCarrito();
    this.mensaje = 'Carrito vaciado correctamente.';
    this.error = '';
  }

  subtotal(item: CarritoItem): number {
    return Number(item.precio || 0) * Number(item.cantidad || 0);
  }

  totalCarrito(): number {
    return this.carritoService.total();
  }

  cantidadTotal(): number {
    return this.carritoService.cantidadTotal();
  }

  confirmarPedido(): void {
    this.cargarCarrito();

    if (!this.auth.estaAutenticado()) {
      this.error = 'Debes iniciar sesión para confirmar el pedido.';
      this.router.navigate(['/login']);
      return;
    }

    if (this.items.length === 0) {
      this.error = 'El carrito está vacío.';
      return;
    }

    const itemInvalido = this.items.find(item =>
      !item.productoId ||
      item.cantidad <= 0 ||
      item.cantidad > item.stock
    );

    if (itemInvalido) {
      this.error = `Revisa la cantidad del producto "${itemInvalido.nombre}".`;
      return;
    }

    this.procesando = true;
    this.mensaje = '';
    this.error = '';

    const request = {
      items: this.items.map(item => ({
        productoId: Number(item.productoId),
        cantidad: Number(item.cantidad)
      }))
    };

    this.pedidoService.checkout(request).subscribe({
      next: (pedidoGenerado) => {
        this.carritoService.vaciar();
        this.cargarCarrito();

        this.mensaje = `Pedido N.º ${pedidoGenerado.id} generado correctamente.`;
        this.error = '';
        this.procesando = false;

        this.router.navigate(['/pedidos']);
      },
      error: (err: any) => {
        console.error('Error al confirmar pedido:', err);

        this.error =
          err?.error?.mensaje ||
          err?.error?.message ||
          'No se pudo confirmar el pedido. Verifica tu sesión, el stock disponible o que el backend esté ejecutándose.';

        this.procesando = false;
      }
    });
  }
}