import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';
import { AppContextService } from './core/app-context.service';
import { readHttpError } from './core/http-error.util';
import { UserRole } from './core/models';

type NavItem = {
  path: string;
  label: string;
  icon: string;
  roles: UserRole[];
};

const NAV_ITEMS: NavItem[] = [
  { path: '/overview', label: 'Visao Geral', icon: 'grid_view', roles: ['DONOR', 'INSTITUTION', 'ADMIN'] },
  { path: '/medications', label: 'Medicamentos', icon: 'inventory_2', roles: ['DONOR', 'INSTITUTION', 'ADMIN'] },
  { path: '/matches', label: 'Doacoes', icon: 'volunteer_activism', roles: ['DONOR', 'ADMIN'] },
  { path: '/queue', label: 'Fila Institucional', icon: 'local_hospital', roles: ['INSTITUTION', 'ADMIN'] },
  { path: '/admin', label: 'Administracao', icon: 'shield', roles: ['ADMIN'] },
];

@Component({
  selector: 'app-root',
  imports: [CommonModule, ReactiveFormsModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  protected readonly auth = inject(AuthService);
  protected readonly context = inject(AppContextService);

  protected readonly loginState = signal<{ error: string | null; loading: boolean }>({
    error: null,
    loading: false,
  });

  protected readonly navigationItems = computed(() => {
    const role = this.auth.role();
    return NAV_ITEMS.filter((item) => role && item.roles.includes(role));
  });

  protected readonly loginForm = this.fb.nonNullable.group({
    username: ['doador', Validators.required],
    password: ['remediar123', Validators.required],
    role: ['DONOR' as UserRole, Validators.required],
  });

  protected readonly demos = [
    { label: 'Doador', username: 'doador', password: 'remediar123', role: 'DONOR' as UserRole },
    { label: 'ONG', username: 'ong', password: 'remediar123', role: 'INSTITUTION' as UserRole },
    { label: 'Admin', username: 'admin', password: 'remediar123', role: 'ADMIN' as UserRole },
  ];

  constructor() {
    if (this.auth.isAuthenticated()) {
      const role = this.auth.role()!;
      this.loginForm.patchValue({
        username: this.auth.username() ?? '',
        password: '',
        role,
      });
    }
  }

  protected login(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const payload = this.loginForm.getRawValue();
    this.loginState.set({ error: null, loading: true });

    this.auth.login({ username: payload.username, password: payload.password }).subscribe({
      next: (response) => {
        this.context.applyRoleDefaults(response.user.role, response.user.actorDocument);
        this.loginState.set({ error: null, loading: false });
        this.router.navigateByUrl(this.initialRouteForRole(response.user.role));
      },
      error: (error) => {
        this.loginState.set({ error: readHttpError(error), loading: false });
      },
    });
  }

  protected applyDemoCredentials(username: string, password: string, role: UserRole): void {
    this.loginForm.patchValue({ username, password, role });
  }

  protected logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/overview');
  }

  protected currentRoleLabel(): string {
    return this.auth.role() === 'DONOR'
      ? 'Doador'
      : this.auth.role() === 'INSTITUTION'
        ? 'Instituicao'
        : 'Administrador';
  }

  private initialRouteForRole(role: UserRole): string {
    if (role === 'INSTITUTION') {
      return '/queue';
    }

    if (role === 'ADMIN') {
      return '/admin';
    }

    return '/medications';
  }
}
