import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import {
  Pedido,
  PedidoDetalleModel,
  PedidoModel,
  PedidoPanelResumen
} from '../../services/pedido';
import { Auth } from '../../services/auth';
import { ConfirmModal } from '../../components/confirm-modal/confirm-modal';
import { Comprobante } from '../../services/comprobante';
import { ComprobanteModel } from '../../services/pago';

@Component({
  selector: 'app-pedidos',
  imports: [CommonModule, FormsModule, RouterLink, ConfirmModal],
  templateUrl: './pedidos.html',
  styleUrl: './pedidos.css',
})
export class Pedidos implements OnInit {
  private pedidoService = inject(Pedido);
  private comprobanteService = inject(Comprobante);

  auth = inject(Auth);

  pedidos: PedidoModel[] = [];
  panelResumen: PedidoPanelResumen | null = null;

  pedidoAEliminar: PedidoModel | null = null;
  pedidoACancelar: PedidoModel | null = null;
  estadoSeleccionado: Record<number, string> = {};
  comprobantesPorPedido: Record<number, ComprobanteModel> = {};

  clienteBusqueda = '';
  estadoBusqueda = '';

  cargando = false;
  cargandoPanel = false;
  mensaje = '';
  error = '';

  estados = ['PENDIENTE', 'EN_PROCESO', 'ENTREGADO', 'CANCELADO'];

  ngOnInit(): void {
    this.cargarPedidos();
  }

  cargarPedidos(): void {
    this.cargando = true;
    this.mensaje = '';
    this.error = '';

    const consulta = this.auth.esAdmin()
      ? this.pedidoService.listar()
      : this.pedidoService.listarMisPedidos();

    consulta.subscribe({
      next: (data: PedidoModel[]) => {
        this.pedidos = data;
        this.inicializarEstados();
        this.cargando = false;
        this.cargarComprobantes();

        if (this.auth.esAdmin()) {
          this.cargarPanelAdministrativo();
        }
      },
      error: () => {
        this.error = this.auth.esAdmin()
          ? 'No se pudieron cargar los pedidos. Verifica tu sesión ADMIN.'
          : 'No se pudieron cargar tus pedidos. Verifica tu sesión.';

        this.cargando = false;
      }
    });
  }

  cargarPanelAdministrativo(): void {
    if (!this.auth.esAdmin()) {
      return;
    }

    this.cargandoPanel = true;

    this.pedidoService.obtenerPanelResumen().subscribe({
      next: (resumen: PedidoPanelResumen) => {
        this.panelResumen = resumen;
        this.cargandoPanel = false;
      },
      error: () => {
        this.panelResumen = null;
        this.cargandoPanel = false;
      }
    });
  }

  cargarComprobantes(): void {
    if (!this.auth.estaAutenticado()) {
      this.comprobantesPorPedido = {};
      return;
    }

    const consulta = this.auth.esAdmin()
      ? this.comprobanteService.listar()
      : this.comprobanteService.listarMisComprobantes();

    consulta.subscribe({
      next: (comprobantes: ComprobanteModel[]) => {
        this.comprobantesPorPedido = {};

        comprobantes.forEach((comprobante) => {
          if (comprobante.pedidoId) {
            this.comprobantesPorPedido[comprobante.pedidoId] = comprobante;
          }
        });
      },
      error: () => {
        this.comprobantesPorPedido = {};
      }
    });
  }

  inicializarEstados(): void {
    this.estadoSeleccionado = {};

    this.pedidos.forEach((pedido) => {
      if (pedido.id) {
        this.estadoSeleccionado[pedido.id] = this.normalizarEstado(pedido.estado);
      }
    });
  }

  seleccionarEstado(pedido: PedidoModel, estado: string): void {
    if (!pedido.id) {
      return;
    }

    this.estadoSeleccionado[pedido.id] = estado;
  }

  actualizarEstadoPedido(pedido: PedidoModel): void {
    if (!this.auth.esAdmin()) {
      this.error = 'Solo un usuario ADMIN puede actualizar el estado de pedidos.';
      return;
    }

    if (!pedido.id) {
      return;
    }

    const nuevoEstado = this.estadoSeleccionado[pedido.id];

    if (!nuevoEstado || nuevoEstado === this.normalizarEstado(pedido.estado)) {
      this.error = 'Selecciona un estado diferente para actualizar el pedido.';
      return;
    }

    this.pedidoService.actualizarEstado(pedido.id, { estado: nuevoEstado }).subscribe({
      next: (response) => {
        this.mensaje = response.mensaje || 'Estado actualizado correctamente.';
        this.error = '';

        this.pedidos = this.pedidos.map((item) =>
          item.id === pedido.id ? response.pedido : item
        );

        this.inicializarEstados();
        this.cargarPanelAdministrativo();
      },
      error: (err: any) => {
        this.error =
          err?.error?.mensaje ||
          'No se pudo actualizar el estado del pedido.';

        this.inicializarEstados();
      }
    });
  }

  buscarPorCliente(): void {
    if (!this.auth.esAdmin()) {
      return;
    }

    const cliente = this.clienteBusqueda.trim();

    if (!cliente) {
      this.cargarPedidos();
      return;
    }

    this.cargando = true;
    this.mensaje = '';
    this.error = '';

    this.pedidoService.buscarPorCliente(cliente).subscribe({
      next: (data: PedidoModel[]) => {
        this.pedidos = data;
        this.inicializarEstados();
        this.cargando = false;
        this.cargarComprobantes();
      },
      error: () => {
        this.error = 'No se pudo buscar pedidos por cliente.';
        this.cargando = false;
      }
    });
  }

  buscarPorEstado(): void {
    if (!this.auth.esAdmin()) {
      return;
    }

    const estado = this.estadoBusqueda.trim();

    if (!estado) {
      this.cargarPedidos();
      return;
    }

    this.cargando = true;
    this.mensaje = '';
    this.error = '';

    this.pedidoService.buscarPorEstado(estado).subscribe({
      next: (data: PedidoModel[]) => {
        this.pedidos = data;
        this.inicializarEstados();
        this.cargando = false;
        this.cargarComprobantes();
      },
      error: () => {
        this.error = 'No se pudo buscar pedidos por estado.';
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

  abrirConfirmacionCancelar(pedido: PedidoModel): void {
    if (!this.puedeCancelarPedido(pedido)) {
      this.error = 'Este pedido no puede cancelarse porque no está pendiente o ya tiene pago registrado.';
      return;
    }

    this.pedidoACancelar = pedido;
  }

  cerrarConfirmacionCancelar(): void {
    this.pedidoACancelar = null;
  }

  confirmarCancelacionPedido(): void {
    const id = this.pedidoACancelar?.id;

    if (!id) {
      return;
    }

    this.pedidoService.cancelarPedido(id).subscribe({
      next: (response) => {
        this.mensaje = response.mensaje || 'Pedido cancelado correctamente.';
        this.error = '';

        this.pedidos = this.pedidos.map((pedido) =>
          pedido.id === id ? response.pedido : pedido
        );

        this.pedidoACancelar = null;
        this.inicializarEstados();
        this.cargarComprobantes();

        if (this.auth.esAdmin()) {
          this.cargarPanelAdministrativo();
        }
      },
      error: (err: any) => {
        this.error =
          err?.error?.mensaje ||
          'No se pudo cancelar el pedido. Verifica el estado del pedido.';

        this.pedidoACancelar = null;
      }
    });
  }

  confirmarEliminacion(): void {
    const id = this.pedidoAEliminar?.id;

    if (!id) {
      return;
    }

    this.pedidoService.eliminar(id).subscribe({
      next: () => {
        this.pedidos = this.pedidos.filter((pedido) => pedido.id !== id);
        this.mensaje = 'Pedido eliminado correctamente.';
        this.error = '';
        this.pedidoAEliminar = null;
        this.cargarPanelAdministrativo();
        this.cargarComprobantes();
      },
      error: () => {
        this.error = 'No se pudo eliminar el pedido. Verifica tu sesión ADMIN.';
        this.pedidoAEliminar = null;
      }
    });
  }

  estadosPermitidos(pedido: PedidoModel): string[] {
    const estado = this.normalizarEstado(pedido.estado);

    if (estado === 'PENDIENTE') {
      return ['EN_PROCESO', 'CANCELADO'];
    }

    if (estado === 'EN_PROCESO') {
      return ['ENTREGADO', 'CANCELADO'];
    }

    return [];
  }

  puedeGestionarEstado(pedido: PedidoModel): boolean {
    return this.auth.esAdmin() && this.estadosPermitidos(pedido).length > 0;
  }

  tieneComprobante(pedido: PedidoModel): boolean {
    if (!pedido.id) {
      return false;
    }

    return !!this.comprobantesPorPedido[pedido.id];
  }

  puedePagarPedido(pedido: PedidoModel): boolean {
    const estado = this.normalizarEstado(pedido.estado);

    return !this.auth.esAdmin()
      && estado !== 'CANCELADO'
      && !this.tieneComprobante(pedido);
  }

  puedeCancelarPedido(pedido: PedidoModel): boolean {
    const estado = this.normalizarEstado(pedido.estado);

    return !this.auth.esAdmin()
      && estado === 'PENDIENTE'
      && !this.tieneComprobante(pedido);
  }

  puedeVerComprobante(pedido: PedidoModel): boolean {
    return !!pedido.id && this.tieneComprobante(pedido);
  }

  estadoClass(estado: string): string {
    const valor = this.normalizarEstado(estado);

    if (valor === 'PENDIENTE') {
      return 'status-pending';
    }

    if (valor === 'EN_PROCESO') {
      return 'status-process';
    }

    if (valor === 'ENTREGADO' || valor === 'COMPLETADO') {
      return 'status-delivered';
    }

    if (valor === 'CANCELADO') {
      return 'status-cancelled';
    }

    return 'status-default';
  }

  formatearEstado(estado: string): string {
    return this.normalizarEstado(estado).replace('_', ' ');
  }

  detallesPedido(pedido: PedidoModel): PedidoDetalleModel[] {
    return pedido.detalles || [];
  }

  resumenProductos(pedido: PedidoModel): string {
    const detalles = this.detallesPedido(pedido);

    if (detalles.length > 0) {
      return detalles
        .map((detalle) => `${detalle.productoNombre} x${detalle.cantidad}`)
        .join(', ');
    }

    if (pedido.productoNombre) {
      return `${pedido.productoNombre} x${pedido.cantidad || 1}`;
    }

    return 'Sin detalle de productos';
  }

  cantidadTotalItems(pedido: PedidoModel): number {
    const detalles = this.detallesPedido(pedido);

    if (detalles.length > 0) {
      return detalles.reduce((total, detalle) => total + Number(detalle.cantidad || 0), 0);
    }

    return Number(pedido.cantidad || 0);
  }

  totalPedidosPanel(): number {
    return this.panelResumen?.totalPedidos ?? this.pedidos.length;
  }

  pendientesPanel(): number {
    return this.panelResumen?.pedidosPendientes ??
      this.pedidos.filter((pedido) => this.normalizarEstado(pedido.estado) === 'PENDIENTE').length;
  }

  enProcesoPanel(): number {
    return this.panelResumen?.pedidosEnProceso ??
      this.pedidos.filter((pedido) => this.normalizarEstado(pedido.estado) === 'EN_PROCESO').length;
  }

  entregadosPanel(): number {
    return this.panelResumen?.pedidosEntregados ??
      this.pedidos.filter((pedido) => {
        const estado = this.normalizarEstado(pedido.estado);
        return estado === 'ENTREGADO' || estado === 'COMPLETADO';
      }).length;
  }

  canceladosPanel(): number {
    return this.panelResumen?.pedidosCancelados ??
      this.pedidos.filter((pedido) => this.normalizarEstado(pedido.estado) === 'CANCELADO').length;
  }

  montoVendidoPanel(): number {
    return this.panelResumen?.montoTotalVendido ??
      this.pedidos
        .filter((pedido) => {
          const estado = this.normalizarEstado(pedido.estado);
          return estado === 'ENTREGADO' || estado === 'COMPLETADO';
        })
        .reduce((total, pedido) => total + Number(pedido.total || 0), 0);
  }

  montoPendientePanel(): number {
    return this.panelResumen?.montoPendiente ??
      this.pedidos
        .filter((pedido) => {
          const estado = this.normalizarEstado(pedido.estado);
          return estado === 'PENDIENTE' || estado === 'EN_PROCESO';
        })
        .reduce((total, pedido) => total + Number(pedido.total || 0), 0);
  }

  inicialCliente(cliente: string): string {
    return cliente ? cliente.charAt(0).toUpperCase() : 'C';
  }

  private normalizarEstado(estado: string | null | undefined): string {
    if (!estado) {
      return 'PENDIENTE';
    }

    return estado.trim().toUpperCase();
  }
}