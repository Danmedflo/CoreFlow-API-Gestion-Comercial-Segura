import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Producto, ProductoModel } from '../../services/producto';

@Component({
  selector: 'app-productos',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './productos.html',
  styleUrl: './productos.css',
})
export class Productos implements OnInit {
  private productoService = inject(Producto);

  productos: ProductoModel[] = [];
  nombreBusqueda = '';
  categoriaBusqueda = '';

  cargando = false;
  mensaje = '';
  error = '';

  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos(): void {
    this.cargando = true;
    this.mensaje = '';
    this.error = '';

    this.productoService.listar().subscribe({
      next: (data) => {
        this.productos = data;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar los productos. Verifica que el backend esté ejecutándose en localhost:8080.';
        this.cargando = false;
      }
    });
  }

  buscarPorNombre(): void {
    const nombre = this.nombreBusqueda.trim();

    if (!nombre) {
      this.cargarProductos();
      return;
    }

    this.cargando = true;
    this.mensaje = '';
    this.error = '';

    this.productoService.buscarPorNombre(nombre).subscribe({
      next: (data) => {
        this.productos = data;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo buscar el producto por nombre.';
        this.cargando = false;
      }
    });
  }

  buscarPorCategoria(): void {
    const categoria = this.categoriaBusqueda.trim();

    if (!categoria) {
      this.cargarProductos();
      return;
    }

    this.cargando = true;
    this.mensaje = '';
    this.error = '';

    this.productoService.buscarPorCategoria(categoria).subscribe({
      next: (data) => {
        this.productos = data;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo buscar el producto por categoría.';
        this.cargando = false;
      }
    });
  }

  limpiarFiltros(): void {
    this.nombreBusqueda = '';
    this.categoriaBusqueda = '';
    this.cargarProductos();
  }

  eliminarProducto(id?: number): void {
    if (!id) {
      return;
    }

    const confirmar = confirm('¿Seguro que deseas eliminar este producto?');

    if (!confirmar) {
      return;
    }

    this.productoService.eliminar(id).subscribe({
      next: () => {
        this.mensaje = 'Producto eliminado correctamente.';
        this.cargarProductos();
      },
      error: () => {
        this.error = 'No se pudo eliminar el producto.';
      }
    });
  }
}