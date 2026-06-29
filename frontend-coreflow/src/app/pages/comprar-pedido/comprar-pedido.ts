import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Producto, ProductoModel } from '../../services/producto';
import { Pedido } from '../../services/pedido';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-comprar-pedido',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './comprar-pedido.html',
  styleUrl: './comprar-pedido.css',
})
export class ComprarPedido implements OnInit {
  private productoService = inject(Producto);
  private pedidoService = inject(Pedido);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  auth = inject(Auth);

  producto: ProductoModel | null = null;

  cliente = '';
  cantidad = 1;

  cargando = false;
  procesando = false;
  mensaje = '';
  error = '';

  ngOnInit(): void {
    this.cliente = this.auth.obtenerUsuario() || '';

    const productoId = Number(this.route.snapshot.paramMap.get('productoId'));

    if (!productoId) {
      this.error = 'No se encontró el producto seleccionado.';
      return;
    }

    this.cargarProducto(productoId);
  }

  cargarProducto(id: number): void {
    this.cargando = true;
    this.error = '';

    this.productoService.obtenerPorId(id).subscribe({
      next: (data) => {
        this.producto = data;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar el producto seleccionado.';
        this.cargando = false;
      }
    });
  }

  totalCompra(): number {
    if (!this.producto) {
      return 0;
    }

    return Number(this.producto.precio || 0) * Number(this.cantidad || 0);
  }

  stockDisponible(): number {
    return Number(this.producto?.stock || 0);
  }

  puedeComprar(): boolean {
    return !!this.producto && this.stockDisponible() > 0;
  }

  inicialProducto(): string {
    return this.producto?.nombre ? this.producto.nombre.charAt(0).toUpperCase() : 'P';
  }

  confirmarCompra(form: NgForm): void {
    if (form.invalid) {
      this.error = 'Completa correctamente los datos de compra.';
      return;
    }

    if (!this.producto?.id) {
      this.error = 'Producto inválido.';
      return;
    }

    if (this.cantidad <= 0) {
      this.error = 'La cantidad debe ser mayor a cero.';
      return;
    }

    if (this.cantidad > this.stockDisponible()) {
      this.error = 'La cantidad solicitada supera el stock disponible.';
      return;
    }

    this.procesando = true;
    this.error = '';
    this.mensaje = '';

    this.pedidoService.comprar({
      cliente: this.cliente.trim(),
      productoId: this.producto.id,
      cantidad: this.cantidad
    }).subscribe({
      next: () => {
        this.mensaje = 'Pedido generado correctamente. El stock fue actualizado.';
        this.procesando = false;

        setTimeout(() => {
          this.router.navigate(['/pedidos']);
        }, 900);
      },
      error: () => {
        this.error = 'No se pudo generar el pedido. Verifica tu sesión o el stock disponible.';
        this.procesando = false;
      }
    });
  }
}