import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Auth } from '../../services/auth';
import { Producto } from '../../services/producto';
import { Pedido } from '../../services/pedido';

@Component({
  selector: 'app-home',
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  auth = inject(Auth);
  private productoService = inject(Producto);
  private pedidoService = inject(Pedido);

  totalProductos = 0;
  totalPedidos = 0;
  cargando = true;

  ngOnInit(): void {
    this.cargarResumen();
  }

  cargarResumen(): void {
    this.cargando = true;

    this.productoService.listar().subscribe({
      next: (productos) => {
        this.totalProductos = productos.length;
      },
      error: () => {
        this.totalProductos = 0;
      }
    });

    this.pedidoService.listar().subscribe({
      next: (pedidos) => {
        this.totalPedidos = pedidos.length;
        this.cargando = false;
      },
      error: () => {
        this.totalPedidos = 0;
        this.cargando = false;
      }
    });
  }
}