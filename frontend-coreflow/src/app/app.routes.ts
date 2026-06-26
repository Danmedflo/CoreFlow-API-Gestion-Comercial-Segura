import { Routes } from '@angular/router';

import { Home } from './pages/home/home';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { Productos } from './pages/productos/productos';
import { ProductoForm } from './pages/producto-form/producto-form';
import { Pedidos } from './pages/pedidos/pedidos';
import { PedidoForm } from './pages/pedido-form/pedido-form';
import { NotFound } from './pages/not-found/not-found';

export const routes: Routes = [
  { path: '', component: Home, title: 'Inicio' },

  { path: 'login', component: Login, title: 'Login' },
  { path: 'registro', component: Register, title: 'Registro' },

  { path: 'productos', component: Productos, title: 'Productos' },
  { path: 'productos/nuevo', component: ProductoForm, title: 'Nuevo producto' },
  { path: 'productos/editar/:id', component: ProductoForm, title: 'Editar producto' },

  { path: 'pedidos', component: Pedidos, title: 'Pedidos' },
  { path: 'pedidos/nuevo', component: PedidoForm, title: 'Nuevo pedido' },
  { path: 'pedidos/editar/:id', component: PedidoForm, title: 'Editar pedido' },

  { path: '**', component: NotFound, title: 'Página no encontrada' }
];