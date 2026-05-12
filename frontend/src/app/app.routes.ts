import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'overview',
  },
  {
    path: 'overview',
    loadComponent: () => import('./features/overview/overview-page').then((m) => m.OverviewPageComponent),
  },
  {
    path: 'medications',
    loadComponent: () => import('./features/medications/medications-page').then((m) => m.MedicationsPageComponent),
  },
  {
    path: 'matches',
    loadComponent: () => import('./features/matches/matches-page').then((m) => m.MatchesPageComponent),
  },
  {
    path: 'queue',
    loadComponent: () => import('./features/queue/institution-queue-page').then((m) => m.InstitutionQueuePageComponent),
  },
  {
    path: 'admin',
    loadComponent: () => import('./features/admin/admin-page').then((m) => m.AdminPageComponent),
  },
  {
    path: '**',
    redirectTo: 'overview',
  },
];
