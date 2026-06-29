import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AuthRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  rol: string;
}

export interface RegistroUsuario {
  username: string;
  password: string;
  rol: string;
  activo?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class Auth {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/auth';

  login(data: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, data);
  }

  register(usuario: RegistroUsuario): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, usuario);
  }

  guardarSesion(response: AuthResponse): void {
    localStorage.setItem('token', response.token);
    localStorage.setItem('username', response.username);
    localStorage.setItem('rol', response.rol);
  }

  obtenerToken(): string | null {
    return localStorage.getItem('token');
  }

  obtenerUsuario(): string | null {
    return localStorage.getItem('username');
  }

  obtenerRol(): string | null {
    return localStorage.getItem('rol');
  }

  estaAutenticado(): boolean {
    return !!this.obtenerToken();
  }

  cerrarSesion(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('rol');
  }

  esAdmin(): boolean {
  return this.obtenerRol() === 'ADMIN';
}
}