import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { Auth } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private authService = inject(Auth);
  private router = inject(Router);

  username = '';
  password = '';

  cargando = false;
  error = '';
  mensaje = '';

  onSubmit(form: NgForm): void {
    if (form.invalid) {
      this.error = 'Completa usuario y contraseña.';
      return;
    }

    this.cargando = true;
    this.error = '';
    this.mensaje = '';

    this.authService.login({
      username: this.username,
      password: this.password
    }).subscribe({
      next: (response) => {
        this.authService.guardarSesion(response);
        this.mensaje = 'Inicio de sesión correcto.';
        this.cargando = false;

        setTimeout(() => {
          this.router.navigate(['/productos']);
        }, 600);
      },
      error: () => {
        this.error = 'Credenciales incorrectas. Verifica tu usuario y contraseña.';
        this.cargando = false;
      }
    });
  }
}