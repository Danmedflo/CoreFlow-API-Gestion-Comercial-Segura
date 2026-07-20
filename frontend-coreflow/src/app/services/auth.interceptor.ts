import { HttpInterceptorFn } from '@angular/common/http';

function tokenExpirado(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));

    if (!payload.exp) {
      return true;
    }

    return Date.now() >= payload.exp * 1000;
  } catch {
    return true;
  }
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');

  if (!token || tokenExpirado(token)) {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('rol');
    localStorage.removeItem('nombreCompleto');

    return next(req);
  }

  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(authReq);
};