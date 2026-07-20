import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Producto, ProductoModel, ProductoPanelResumen } from '../../services/producto';
import { Auth } from '../../services/auth';
import { Carrito } from '../../services/carrito';
import { ConfirmModal } from '../../components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-productos',
  imports: [CommonModule, FormsModule, RouterLink, ConfirmModal],
  templateUrl: './productos.html',
  styleUrl: './productos.css',
})
export class Productos implements OnInit {
  private productoService = inject(Producto);
  private carritoService = inject(Carrito);

  auth = inject(Auth);

  productos: ProductoModel[] = [];
  productosStockCritico: ProductoModel[] = [];
  panelResumen: ProductoPanelResumen | null = null;

  productoAEliminar: ProductoModel | null = null;

  nombreBusqueda = '';
  categoriaBusqueda = '';
  limiteStockCritico = 5;

  cargando = false;
  cargandoPanel = false;
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

        if (this.auth.esAdmin()) {
          this.cargarPanelAdministrativo();
        }
      },
      error: () => {
        this.error = 'No se pudieron cargar los productos. Verifica que el backend esté ejecutándose.';
        this.cargando = false;
      }
    });
  }

  cargarPanelAdministrativo(): void {
    if (!this.auth.esAdmin()) {
      return;
    }

    this.cargandoPanel = true;

    this.productoService.obtenerPanelResumen().subscribe({
      next: (resumen) => {
        this.panelResumen = resumen;
        this.cargandoPanel = false;
      },
      error: () => {
        this.panelResumen = null;
        this.cargandoPanel = false;
      }
    });

    this.productoService.listarStockCritico(this.limiteStockCritico).subscribe({
      next: (data) => {
        this.productosStockCritico = data;
      },
      error: () => {
        this.productosStockCritico = [];
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

  agregarAlCarrito(producto: ProductoModel): void {
    this.mensaje = '';
    this.error = '';

    if (!this.auth.estaAutenticado()) {
      this.error = 'Inicia sesión para agregar productos al carrito.';
      return;
    }

    if (this.auth.esAdmin()) {
      this.error = 'El carrito está disponible para usuarios compradores. El ADMIN gestiona productos y pedidos.';
      return;
    }

    try {
      this.carritoService.agregarProducto(producto, 1);
      this.mensaje = `Producto "${producto.nombre}" agregado al carrito.`;
    } catch (error: any) {
      this.error = error.message || 'No se pudo agregar el producto al carrito.';
    }
  }

  cantidadEnCarrito(producto: ProductoModel): number {
    if (!producto.id) {
      return 0;
    }

    const item = this.carritoService.obtenerItems()
      .find(productoCarrito => productoCarrito.productoId === producto.id);

    return item ? item.cantidad : 0;
  }

  abrirConfirmacionEliminar(producto: ProductoModel): void {
    if (!this.auth.esAdmin()) {
      this.error = 'Solo un usuario ADMIN puede eliminar productos.';
      return;
    }

    this.productoAEliminar = producto;
  }

  cancelarEliminacion(): void {
    this.productoAEliminar = null;
  }

  confirmarEliminacion(): void {
    const id = this.productoAEliminar?.id;

    if (!id) {
      return;
    }

    const nombre = this.productoAEliminar?.nombre || 'Producto';

    this.productoService.eliminar(id).subscribe({
      next: () => {
        this.productos = this.productos.filter(producto => producto.id !== id);
        this.mensaje = `Producto "${nombre}" eliminado correctamente.`;
        this.error = '';
        this.productoAEliminar = null;
        this.cargarPanelAdministrativo();
      },
      error: () => {
        this.error = 'No se pudo eliminar el producto. Verifica que hayas iniciado sesión como ADMIN.';
        this.productoAEliminar = null;
      }
    });
  }

  totalProductosPanel(): number {
    return this.panelResumen?.totalProductos ?? this.productos.length;
  }

  stockTotalPanel(): number {
    return this.panelResumen?.stockTotal ?? this.totalStock();
  }

  stockCriticoPanel(): number {
    return this.panelResumen?.productosStockCritico ?? this.productos.filter(producto => producto.stock > 0 && producto.stock <= 5).length;
  }

  sinStockPanel(): number {
    return this.panelResumen?.productosSinStock ?? this.productos.filter(producto => producto.stock <= 0).length;
  }

  valorInventarioPanel(): number {
    return this.panelResumen?.valorInventario ?? this.valorInventario();
  }

  totalStock(): number {
    return this.productos.reduce((total, producto) => total + Number(producto.stock || 0), 0);
  }

  valorInventario(): number {
    return this.productos.reduce((total, producto) => {
      return total + Number(producto.precio || 0) * Number(producto.stock || 0);
    }, 0);
  }

  stockClass(producto: ProductoModel): string {
    if (producto.stock <= 0) {
      return 'stock-danger';
    }

    if (producto.stock <= 5) {
      return 'stock-warning';
    }

    return 'stock-success';
  }

  stockTexto(producto: ProductoModel): string {
    if (producto.stock <= 0) {
      return 'Sin stock';
    }

    if (producto.stock <= 5) {
      return 'Stock bajo';
    }

    return 'Disponible';
  }

  inicialProducto(nombre: string): string {
    return nombre ? nombre.charAt(0).toUpperCase() : 'P';
  }
}