import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith('/api/') || request.url === '/api/v1/auth/login') {
    return next(request);
  }

  const authHeader = inject(AuthService).authHeader();
  if (!authHeader) {
    return next(request);
  }

  return next(request.clone({
    setHeaders: {
      Authorization: authHeader,
    },
  }));
};
