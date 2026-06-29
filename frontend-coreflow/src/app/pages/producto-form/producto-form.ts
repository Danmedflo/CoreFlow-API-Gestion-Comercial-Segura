import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Producto, ProductoModel } from '../../services/producto';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-producto-form',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './producto-form.html',
  styleUrl: './producto-form.css',
})
export class ProductoForm implements OnInit {
  private productoService = inject(Producto);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  auth = inject(Auth);

  producto: ProductoModel = {
    nombre: '',
    precio: 0,
    stock: 0,
    categoria: '',
    descripcion: ''
  };

  idProducto?: number;
  modoEdicion = false;

  cargando = false;
  mensaje = '';
  error = '';

  ngOnInit(): void {
    if (!this.auth.esAdmin()) {
      this.error = 'Acceso denegado. Solo un usuario ADMIN puede crear o editar productos.';
      return;
    }

    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.idProducto = Number(id);
      this.modoEdicion = true;
      this.cargarProducto(this.idProducto);
    }
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

  guardarProducto(form: NgForm): void {
    if (!this.auth.esAdmin()) {
      this.error = 'Solo un usuario ADMIN puede guardar productos.';
      return;
    }

    if (form.invalid) {
      this.error = 'Completa correctamente todos los campos requeridos.';
      return;
    }

    this.cargando = true;
    this.error = '';
    this.mensaje = '';

    if (this.modoEdicion && this.idProducto) {
      this.productoService.actualizar(this.idProducto, this.producto).subscribe({
        next: () => {
          this.mensaje = 'Producto actualizado correctamente.';
          this.cargando = false;

          setTimeout(() => {
            this.router.navigate(['/productos']);
          }, 700);
        },
        error: () => {
          this.error = 'No se pudo actualizar el producto. Verifica tu sesión ADMIN.';
          this.cargando = false;
        }
      });

      return;
    }

    this.productoService.crear(this.producto).subscribe({
      next: () => {
        this.mensaje = 'Producto creado correctamente.';
        this.cargando = false;

        setTimeout(() => {
          this.router.navigate(['/productos']);
        }, 700);
      },
      error: () => {
        this.error = 'No se pudo crear el producto. Verifica tu sesión ADMIN.';
        this.cargando = false;
      }
    });
  }

  valorInventario(): number {
    return Number(this.producto.precio || 0) * Number(this.producto.stock || 0);
  }

  estadoStockTexto(): string {
    if (this.producto.stock <= 0) {
      return 'Sin stock';
    }

    if (this.producto.stock <= 5) {
      return 'Stock bajo';
    }

    return 'Disponible';
  }

  estadoStockClase(): string {
    if (this.producto.stock <= 0) {
      return 'status-danger';
    }

    if (this.producto.stock <= 5) {
      return 'status-warning';
    }

    return 'status-success';
  }

  inicialProducto(): string {
    return this.producto.nombre ? this.producto.nombre.charAt(0).toUpperCase() : 'P';
  }
}