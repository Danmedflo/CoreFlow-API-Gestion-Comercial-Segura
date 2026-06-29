import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Pedido, PedidoModel } from '../../services/pedido';
import { Auth } from '../../services/auth';
import { ConfirmModal } from '../../components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-pedidos',
  imports: [CommonModule, FormsModule, RouterLink, ConfirmModal],
  templateUrl: './pedidos.html',
  styleUrl: './pedidos.css',
})
export class Pedidos implements OnInit {
  private pedidoService = inject(Pedido);
  auth = inject(Auth);

  pedidos: PedidoModel[] = [];
  pedidoAEliminar: PedidoModel | null = null;

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

    const solicitud = this.auth.esAdmin()
      ? this.pedidoService.listar()
      : this.pedidoService.misPedidos();

    solicitud.subscribe({
      next: (data) => {
        this.pedidos = data;
        this.cargando = false;
      },
      error: () => {
        this.error = this.auth.esAdmin()
          ? 'No se pudieron cargar los pedidos del sistema.'
          : 'No se pudieron cargar tus pedidos. Verifica tu sesión.';

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

    if (!this.auth.esAdmin()) {
      this.pedidos = this.pedidos.filter(pedido =>
        pedido.cliente.toLowerCase().includes(cliente.toLowerCase())
      );
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

    if (!this.auth.esAdmin()) {
      this.pedidos = this.pedidos.filter(pedido =>
        pedido.estado.toLowerCase() === estado.toLowerCase()
      );
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

  abrirConfirmacionEliminar(pedido: PedidoModel): void {
    if (!this.auth.esAdmin()) {
      this.error = 'Solo un usuario ADMIN puede eliminar pedidos.';
      return;
    }

    this.pedidoAEliminar = pedido;
  }

  cancelarEliminacion(): void {
    this.pedidoAEliminar = null;
  }

  confirmarEliminacion(): void {
    const id = this.pedidoAEliminar?.id;

    if (!id) {
      return;
    }

    const cliente = this.pedidoAEliminar?.cliente || 'Cliente';

    this.pedidoService.eliminar(id).subscribe({
      next: () => {
        this.pedidos = this.pedidos.filter(pedido => pedido.id !== id);
        this.mensaje = `Pedido de "${cliente}" eliminado correctamente.`;
        this.error = '';
        this.pedidoAEliminar = null;
      },
      error: () => {
        this.error = 'No se pudo eliminar el pedido. Verifica que hayas iniciado sesión como ADMIN.';
        this.pedidoAEliminar = null;
      }
    });
  }

  totalVentas(): number {
    return this.pedidos.reduce((total, pedido) => total + Number(pedido.total || 0), 0);
  }

  pedidosCompletados(): number {
    return this.pedidos.filter(pedido => pedido.estado === 'COMPLETADO').length;
  }

  pedidosPendientes(): number {
    return this.pedidos.filter(pedido => pedido.estado === 'PENDIENTE').length;
  }

  estadoClass(estado: string): string {
    switch (estado) {
      case 'COMPLETADO':
        return 'status-success';
      case 'EN_PROCESO':
        return 'status-info';
      case 'CANCELADO':
        return 'status-danger';
      default:
        return 'status-warning';
    }
  }

  inicialCliente(cliente: string): string {
    return cliente ? cliente.charAt(0).toUpperCase() : 'C';
  }
}