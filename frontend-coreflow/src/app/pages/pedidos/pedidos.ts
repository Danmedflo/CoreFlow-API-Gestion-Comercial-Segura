import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Pedido, PedidoModel } from '../../services/pedido';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-pedidos',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pedidos.html',
  styleUrl: './pedidos.css',
})
export class Pedidos implements OnInit {
  private pedidoService = inject(Pedido);
  auth = inject(Auth);

  pedidos: PedidoModel[] = [];
  clienteBusqueda = '';
  estadoBusqueda = '';

  cargando = false;
  mensaje = '';
  error = '';

  ngOnInit(): void {
    this.cargarPedidos();
  }

  cargarPedidos(): void {
    this.cargando = true;
    this.mensaje = '';
    this.error = '';

    this.pedidoService.listar().subscribe({
      next: (data) => {
        this.pedidos = data;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar los pedidos. Verifica que el backend esté ejecutándose.';
        this.cargando = false;
      }
    });
  }

  buscarPorCliente(): void {
    const cliente = this.clienteBusqueda.trim();

    if (!cliente) {
      this.cargarPedidos();
      return;
    }

    this.cargando = true;
    this.mensaje = '';
    this.error = '';

    this.pedidoService.buscarPorCliente(cliente).subscribe({
      next: (data) => {
        this.pedidos = data;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo buscar el pedido por cliente.';
        this.cargando = false;
      }
    });
  }

  buscarPorEstado(): void {
    const estado = this.estadoBusqueda.trim();

    if (!estado) {
      this.cargarPedidos();
      return;
    }

    this.cargando = true;
    this.mensaje = '';
    this.error = '';

    this.pedidoService.buscarPorEstado(estado).subscribe({
      next: (data) => {
        this.pedidos = data;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo buscar el pedido por estado.';
        this.cargando = false;
      }
    });
  }

  limpiarFiltros(): void {
    this.clienteBusqueda = '';
    this.estadoBusqueda = '';
    this.cargarPedidos();
  }

  eliminarPedido(id?: number): void {
    if (!id) {
      return;
    }

    if (!this.auth.esAdmin()) {
      this.error = 'Solo un usuario ADMIN puede eliminar pedidos.';
      return;
    }

    const confirmar = confirm('¿Seguro que deseas eliminar este pedido?');

    if (!confirmar) {
      return;
    }

    this.pedidoService.eliminar(id).subscribe({
      next: () => {
        this.mensaje = 'Pedido eliminado correctamente.';
        this.cargarPedidos();
      },
      error: () => {
        this.error = 'No se pudo eliminar el pedido. Verifica que hayas iniciado sesión como ADMIN.';
      }
    });
  }
}