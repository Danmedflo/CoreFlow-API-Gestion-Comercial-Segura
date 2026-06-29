import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Perfil, PerfilUsuario } from '../../services/perfil';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-perfil',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class PerfilPage implements OnInit {
  private perfilService = inject(Perfil);
  auth = inject(Auth);

  perfil: PerfilUsuario = {
    username: '',
    nombreCompleto: '',
    rol: '',
    activo: true
  };

  nuevaPassword = '';
  confirmarPassword = '';

  cargando = false;
  guardando = false;
  mensaje = '';
  error = '';

  ngOnInit(): void {
    this.cargarPerfil();
  }

  cargarPerfil(): void {
    this.cargando = true;
    this.error = '';
    this.mensaje = '';

    this.perfilService.obtenerPerfil().subscribe({
      next: (data) => {
        this.perfil = data;

        if (data.nombreCompleto) {
          this.auth.guardarNombrePerfil(data.nombreCompleto);
        }

        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar la información del perfil.';
        this.cargando = false;
      }
    });
  }

  guardarPerfil(form: NgForm): void {
    if (form.invalid) {
      this.error = 'Completa correctamente los datos del perfil.';
      return;
    }

    if (this.nuevaPassword && this.nuevaPassword !== this.confirmarPassword) {
      this.error = 'Las contraseñas no coinciden.';
      return;
    }

    this.guardando = true;
    this.error = '';
    this.mensaje = '';

    this.perfilService.actualizarPerfil({
      nombreCompleto: this.perfil.nombreCompleto,
      nuevaPassword: this.nuevaPassword || undefined
    }).subscribe({
      next: (data) => {
        this.perfil = data;
        this.auth.guardarNombrePerfil(data.nombreCompleto);

        this.nuevaPassword = '';
        this.confirmarPassword = '';

        this.mensaje = 'Perfil actualizado correctamente.';
        this.guardando = false;
      },
      error: () => {
        this.error = 'No se pudo actualizar el perfil. Verifica los datos ingresados.';
        this.guardando = false;
      }
    });
  }

  inicialUsuario(): string {
    if (this.perfil.nombreCompleto) {
      return this.perfil.nombreCompleto.charAt(0).toUpperCase();
    }

    return this.perfil.username ? this.perfil.username.charAt(0).toUpperCase() : 'U';
  }
}