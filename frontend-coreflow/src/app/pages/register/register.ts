import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Auth } from '../../services/auth';

@Component({
  selector: 'app-register',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private authService = inject(Auth);

  username = '';
  password = '';
  confirmarPassword = '';

  cargando = false;
  mensaje = '';
  error = '';

  onSubmit(form: NgForm): void {
    if (form.invalid) {
      this.error = 'Completa correctamente todos los campos.';
      return;
    }

    if (this.password !== this.confirmarPassword) {
      this.error = 'Las contraseñas no coinciden.';
      return;
    }

    this.cargando = true;
    this.error = '';
    this.mensaje = '';

    this.authService.register({
      username: this.username,
      password: this.password,
      rol: 'USER',
      activo: true
    }).subscribe({
      next: () => {
        this.mensaje = 'Usuario registrado correctamente. Ya puedes iniciar sesión.';
        this.cargando = false;
        form.resetForm();
      },
      error: () => {
        this.error = 'No se pudo registrar el usuario. Puede que el nombre de usuario ya exista.';
        this.cargando = false;
      }
    });
  }

  passwordCoincide(): boolean {
    return this.password === this.confirmarPassword;
  }
}