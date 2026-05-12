import { Injectable, computed, signal } from '@angular/core';
import { OperationalContext, UserRole } from './models';

const STORAGE_KEY = 'remediar-operational-context';

const ROLE_DEFAULTS: Record<UserRole, OperationalContext> = {
  DONOR: {
    donorId: 1,
    donorCpf: '11122233344',
    institutionId: 1,
    actorDocument: '11122233344',
    donorLatitude: -23.55052,
    donorLongitude: -46.633308,
    radiusKm: 10,
  },
  INSTITUTION: {
    donorId: 1,
    donorCpf: '11122233344',
    institutionId: 1,
    actorDocument: '12345678000199',
    donorLatitude: -23.55052,
    donorLongitude: -46.633308,
    radiusKm: 10,
  },
  ADMIN: {
    donorId: 1,
    donorCpf: '11122233344',
    institutionId: 1,
    actorDocument: '99988877766',
    donorLatitude: -23.55052,
    donorLongitude: -46.633308,
    radiusKm: 10,
  },
};

@Injectable({ providedIn: 'root' })
export class AppContextService {
  private readonly state = signal<OperationalContext>(this.loadInitialState());
  readonly context = computed(() => this.state());

  update(partial: Partial<OperationalContext>): void {
    this.state.update((current) => {
      const next = { ...current, ...partial };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      return next;
    });
  }

  applyRoleDefaults(role: UserRole, actorDocument?: string): void {
    const next = {
      ...ROLE_DEFAULTS[role],
      ...(actorDocument ? { actorDocument } : {}),
    };
    this.state.set(next);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
  }

  private loadInitialState(): OperationalContext {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return ROLE_DEFAULTS.DONOR;
    }

    try {
      return { ...ROLE_DEFAULTS.DONOR, ...JSON.parse(raw) } as OperationalContext;
    } catch {
      return ROLE_DEFAULTS.DONOR;
    }
  }
}
