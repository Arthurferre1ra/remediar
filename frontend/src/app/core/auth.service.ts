import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AuthTokenResponse, LoginRequest, SessionState, UserRole } from './models';

const STORAGE_KEY = 'remediar-session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly sessionState = signal<SessionState | null>(this.loadSession());

  readonly session = computed(() => this.sessionState());
  readonly isAuthenticated = computed(() => this.sessionState() !== null);
  readonly role = computed(() => this.sessionState()?.role ?? null);
  readonly username = computed(() => this.sessionState()?.username ?? null);
  readonly authHeader = computed(() => {
    const session = this.sessionState();
    return session ? `${session.tokenType} ${session.accessToken}` : null;
  });

  login(payload: LoginRequest): Observable<AuthTokenResponse> {
    return this.http.post<AuthTokenResponse>('/api/v1/auth/login', payload).pipe(
      tap((response) => {
        const session: SessionState = {
          username: response.user.username,
          role: response.user.role,
          actorDocument: response.user.actorDocument,
          accessToken: response.accessToken,
          tokenType: response.tokenType,
          expiresAt: Date.now() + response.expiresIn * 1000,
        };
        this.sessionState.set(session);
        localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
      }),
    );
  }

  logout(): void {
    this.sessionState.set(null);
    localStorage.removeItem(STORAGE_KEY);
  }

  isRoleAllowed(roles: UserRole[]): boolean {
    const role = this.role();
    return role !== null && roles.includes(role);
  }

  private loadSession(): SessionState | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }

    try {
      const session = JSON.parse(raw) as SessionState;
      if (!session.accessToken || !session.tokenType || !session.expiresAt || session.expiresAt <= Date.now()) {
        localStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return session;
    } catch {
      return null;
    }
  }
}
