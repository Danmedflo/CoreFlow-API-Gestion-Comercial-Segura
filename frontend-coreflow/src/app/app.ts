import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { Auth } from './services/auth';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  auth = inject(Auth);
  private router = inject(Router);

  menuAbierto = false;
  userMenuAbierto = false;

  cambiarMenu(): void {
    this.menuAbierto = !this.menuAbierto;
    this.userMenuAbierto = false;
  }

  cerrarMenu(): void {
    this.menuAbierto = false;
  }

  cambiarUserMenu(): void {
    this.userMenuAbierto = !this.userMenuAbierto;
    this.menuAbierto = false;
  }

  cerrarUserMenu(): void {
    this.userMenuAbierto = false;
  }

  cerrarSesion(): void {
    this.auth.cerrarSesion();
    this.menuAbierto = false;
    this.userMenuAbierto = false;
    this.router.navigate(['/login']);
  }

  nombreVisible(): string {
    const nombrePerfil = this.auth.obtenerNombrePerfil();

    if (nombrePerfil) {
      return nombrePerfil;
    }

    const usuario = this.auth.obtenerUsuario();

    if (!usuario) {
      return 'Usuario';
    }

    const base = usuario.includes('@') ? usuario.split('@')[0] : usuario;

    const limpio = base
      .replace(/[0-9]/g, '')
      .replace(/[._-]/g, ' ')
      .trim();

    if (!limpio) {
      return 'Usuario';
    }

    return limpio
      .split(' ')
      .filter(Boolean)
      .map(parte => parte.charAt(0).toUpperCase() + parte.slice(1).toLowerCase())
      .join(' ');
  }

  inicialUsuario(): string {
    return this.nombreVisible().charAt(0).toUpperCase();
  }
}