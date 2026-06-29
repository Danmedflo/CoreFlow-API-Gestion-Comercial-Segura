import { Routes } from '@angular/router';

import { Home } from './pages/home/home';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { Productos } from './pages/productos/productos';
import { ProductoForm } from './pages/producto-form/producto-form';
import { Pedidos } from './pages/pedidos/pedidos';
import { PedidoForm } from './pages/pedido-form/pedido-form';
import { ComprarPedido } from './pages/comprar-pedido/comprar-pedido';
import { PerfilPage } from './pages/perfil/perfil';
import { NotFound } from './pages/not-found/not-found';

import { adminGuard } from './guards/admin.guard';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', component: Home, title: 'Inicio' },

  { path: 'login', component: Login, title: 'Login' },
  { path: 'registro', component: Register, title: 'Registro' },

  { path: 'productos', component: Productos, title: 'Productos' },
  {
    path: 'productos/nuevo',
    component: ProductoForm,
    title: 'Nuevo producto',
    canActivate: [adminGuard]
  },
  {
    path: 'productos/editar/:id',
    component: ProductoForm,
    title: 'Editar producto',
    canActivate: [adminGuard]
  },

  {
    path: 'pedidos',
    component: Pedidos,
    title: 'Pedidos',
    canActivate: [authGuard]
  },
  {
    path: 'pedidos/comprar/:productoId',
    component: ComprarPedido,
    title: 'Comprar producto',
    canActivate: [authGuard]
  },
  {
    path: 'pedidos/nuevo',
    component: PedidoForm,
    title: 'Nuevo pedido',
    canActivate: [adminGuard]
  },
  {
    path: 'pedidos/editar/:id',
    component: PedidoForm,
    title: 'Editar pedido',
    canActivate: [adminGuard]
  },
  {
    path: 'perfil',
    component: PerfilPage,
    title: 'Editar perfil',
    canActivate: [authGuard]
  },

  { path: '**', component: NotFound, title: 'Página no encontrada' }
];