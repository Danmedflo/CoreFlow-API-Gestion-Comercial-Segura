import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Pedido, PedidoModel } from '../../services/pedido';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-pedido-form',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pedido-form.html',
  styleUrl: './pedido-form.css',
})
export class PedidoForm implements OnInit {
  private pedidoService = inject(Pedido);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  auth = inject(Auth);

  pedido: PedidoModel = {
    cliente: '',
    fecha: new Date().toISOString().slice(0, 10),
    total: 0,
    estado: 'PENDIENTE'
  };

  idPedido?: number;
  modoEdicion = false;

  cargando = false;
  mensaje = '';
  error = '';

  ngOnInit(): void {
    if (!this.auth.esAdmin()) {
      this.error = 'Acceso denegado. Solo un usuario ADMIN puede crear o editar pedidos.';
      return;
    }

    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.idPedido = Number(id);
      this.modoEdicion = true;
      this.cargarPedido(this.idPedido);
    }
  }

  cargarPedido(id: number): void {
    this.cargando = true;
    this.error = '';

    this.pedidoService.obtenerPorId(id).subscribe({
      next: (data) => {
        this.pedido = data;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar el pedido seleccionado.';
        this.cargando = false;
      }
    });
  }

  guardarPedido(form: NgForm): void {
    if (!this.auth.esAdmin()) {
      this.error = 'Solo un usuario ADMIN puede guardar pedidos.';
      return;
    }

    if (form.invalid) {
      this.error = 'Completa correctamente todos los campos.';
      return;
    }

    this.cargando = true;
    this.error = '';
    this.mensaje = '';

    if (this.modoEdicion && this.idPedido) {
      this.pedidoService.actualizar(this.idPedido, this.pedido).subscribe({
        next: () => {
          this.mensaje = 'Pedido actualizado correctamente.';
          this.cargando = false;

          setTimeout(() => {
            this.router.navigate(['/pedidos']);
          }, 700);
        },
        error: () => {
          this.error = 'No se pudo actualizar el pedido. Verifica tu sesión ADMIN.';
          this.cargando = false;
        }
      });

      return;
    }

    this.pedidoService.crear(this.pedido).subscribe({
      next: () => {
        this.mensaje = 'Pedido creado correctamente.';
        this.cargando = false;

        setTimeout(() => {
          this.router.navigate(['/pedidos']);
        }, 700);
      },
      error: () => {
        this.error = 'No se pudo crear el pedido. Verifica tu sesión ADMIN.';
        this.cargando = false;
      }
    });
  }
}