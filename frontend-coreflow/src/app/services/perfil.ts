import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface PerfilUsuario {
  username: string;
  nombreCompleto: string;
  rol: string;
  activo: boolean;
}

export interface ActualizarPerfilRequest {
  nombreCompleto: string;
  nuevaPassword?: string;
}

@Injectable({
  providedIn: 'root'
})
export class Perfil {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/perfil';

  obtenerPerfil(): Observable<PerfilUsuario> {
    return this.http.get<PerfilUsuario>(this.apiUrl);
  }

  actualizarPerfil(request: ActualizarPerfilRequest): Observable<PerfilUsuario> {
    return this.http.put<PerfilUsuario>(this.apiUrl, request);
  }
}