import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Pedido, PedidoModel } from '../../services/pedido';
import { Producto, ProductoModel } from '../../services/producto';

interface PedidoOperativoItem {
  productoId: number | null;
  cantidad: number;
}

@Component({
  selector: 'app-pedido-operativo',
  imports: [CommonModule, FormsModule],
  templateUrl: './pedido-operativo.html',
  styleUrl: './pedido-operativo.css',
})
export class PedidoOperativoPage implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private pedidoService = inject(Pedido);
  private productoService = inject(Producto);

  pedido: PedidoModel | null = null;
  productos: ProductoModel[] = [];
  items: PedidoOperativoItem[] = [];

  cargando = false;
  guardando = false;
  mensaje = '';
  error = '';

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id) {
      this.error = 'No se encontró el identificador del pedido.';
      return;
    }

    this.cargando = true;
    this.error = '';
    this.mensaje = '';

    this.productoService.listar().subscribe({
      next: (productos) => {
        this.productos = productos;
        this.cargarPedido(id);
      },
      error: () => {
        this.error = 'No se pudieron cargar los productos.';
        this.cargando = false;
      }
    });
  }

  cargarPedido(id: number): void {
    this.pedidoService.obtenerPorId(id).subscribe({
      next: (pedido) => {
        this.pedido = pedido;
        this.inicializarItems(pedido);
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar el pedido seleccionado.';
        this.cargando = false;
      }
    });
  }

  inicializarItems(pedido: PedidoModel): void {
    if (pedido.detalles && pedido.detalles.length > 0) {
      this.items = pedido.detalles.map(detalle => ({
        productoId: detalle.productoId,
        cantidad: detalle.cantidad
      }));
      return;
    }

    if (pedido.productoId) {
      this.items = [
        {
          productoId: pedido.productoId,
          cantidad: pedido.cantidad || 1
        }
      ];
      return;
    }

    this.items = [
      {
        productoId: null,
        cantidad: 1
      }
    ];
  }

  agregarProducto(): void {
    this.items.push({
      productoId: null,
      cantidad: 1
    });
  }

  quitarProducto(index: number): void {
    if (this.items.length === 1) {
      this.error = 'El pedido debe tener al menos un producto.';
      return;
    }

    this.items.splice(index, 1);
    this.error = '';
  }

  productoSeleccionado(productoId: number | null): ProductoModel | undefined {
    if (!productoId) {
      return undefined;
    }

    return this.productos.find(producto => producto.id === Number(productoId));
  }

  totalEstimado(): number {
    return this.items.reduce((total, item) => {
      const producto = this.productoSeleccionado(item.productoId);
      return total + Number(producto?.precio || 0) * Number(item.cantidad || 0);
    }, 0);
  }

  puedeEditar(): boolean {
    const estado = this.pedido?.estado?.trim().toUpperCase();

    return estado !== 'ENTREGADO' && estado !== 'CANCELADO';
  }

  guardarCambios(): void {
    if (!this.pedido?.id) {
      this.error = 'No se encontró el pedido.';
      return;
    }

    if (!this.puedeEditar()) {
      this.error = 'No se puede editar un pedido entregado o cancelado.';
      return;
    }

    const itemsValidos = this.items
      .filter(item => item.productoId && Number(item.cantidad) > 0)
      .map(item => ({
        productoId: Number(item.productoId),
        cantidad: Number(item.cantidad)
      }));

    if (itemsValidos.length === 0) {
      this.error = 'Agrega al menos un producto válido al pedido.';
      return;
    }

    this.guardando = true;
    this.error = '';
    this.mensaje = '';

    this.pedidoService.actualizarPedidoOperativo(this.pedido.id, { items: itemsValidos }).subscribe({
      next: (response) => {
        this.pedido = response.pedido;
        this.inicializarItems(response.pedido);
        this.mensaje = response.mensaje || 'Pedido actualizado correctamente.';
        this.error = '';
        this.guardando = false;
      },
      error: (err: any) => {
        this.error =
          err?.error?.mensaje ||
          'No se pudo actualizar el contenido del pedido.';
        this.guardando = false;
      }
    });
  }

  volver(): void {
    this.router.navigate(['/pedidos']);
  }

  estadoFormateado(): string {
    return this.pedido?.estado?.replace('_', ' ') || 'PENDIENTE';
  }
}