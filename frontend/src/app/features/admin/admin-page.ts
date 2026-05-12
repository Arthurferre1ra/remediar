import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../core/auth.service';
import { readHttpError } from '../../core/http-error.util';
import { RemediarApiService } from '../../core/remediar-api.service';

@Component({
  selector: 'app-admin-page',
  imports: [CommonModule],
  templateUrl: './admin-page.html',
  styleUrl: './admin-page.css',
})
export class AdminPageComponent {
  private readonly api = inject(RemediarApiService);

  protected readonly auth = inject(AuthService);
  protected readonly running = signal(false);
  protected readonly result = signal<string | null>(null);

  protected expireOverdue(): void {
    this.running.set(true);
    this.result.set(null);

    this.api.expireOverdueDonations().subscribe({
      next: (count) => {
        this.result.set(`${count} fluxo(s) vencido(s) foram processados.`);
        this.running.set(false);
      },
      error: (error) => {
        this.result.set(readHttpError(error));
        this.running.set(false);
      },
    });
  }
}
