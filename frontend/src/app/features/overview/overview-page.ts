import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { AppContextService } from '../../core/app-context.service';
import { AuthService } from '../../core/auth.service';
import { readHttpError } from '../../core/http-error.util';
import { DonationMatchResponse, MedicationResponse } from '../../core/models';
import { RemediarApiService } from '../../core/remediar-api.service';

@Component({
  selector: 'app-overview-page',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './overview-page.html',
  styleUrl: './overview-page.css',
})
export class OverviewPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(RemediarApiService);

  protected readonly auth = inject(AuthService);
  protected readonly appContext = inject(AppContextService);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly medications = signal<MedicationResponse[]>([]);
  protected readonly queue = signal<DonationMatchResponse[]>([]);

  protected readonly contextForm = this.fb.nonNullable.group({
    donorId: [this.appContext.context().donorId, [Validators.required, Validators.min(1)]],
    donorCpf: [this.appContext.context().donorCpf, [Validators.required, Validators.minLength(11)]],
    institutionId: [this.appContext.context().institutionId, [Validators.required, Validators.min(1)]],
    actorDocument: [this.appContext.context().actorDocument, Validators.required],
    donorLatitude: [this.appContext.context().donorLatitude, Validators.required],
    donorLongitude: [this.appContext.context().donorLongitude, Validators.required],
    radiusKm: [this.appContext.context().radiusKm, [Validators.required, Validators.min(1)]],
  });

  protected readonly metrics = computed(() => {
    const medications = this.medications();
    const queue = this.queue();
    return {
      availableMedications: medications.filter((item) => item.status === 'AVAILABLE').length,
      inProgressMedications: medications.filter((item) => item.status === 'DONATION_IN_PROGRESS').length,
      queueAwaitingAcceptance: queue.filter((item) => item.status === 'AWAITING_ACCEPTANCE').length,
      queueAwaitingDelivery: queue.filter((item) => item.status === 'AWAITING_DELIVERY').length,
    };
  });

  constructor() {
    this.refresh();
  }

  protected saveContext(): void {
    if (this.contextForm.invalid) {
      this.contextForm.markAllAsTouched();
      return;
    }

    this.appContext.update(this.contextForm.getRawValue());
    this.refresh();
  }

  protected refresh(): void {
    if (!this.auth.isAuthenticated()) {
      return;
    }

    const context = this.appContext.context();
    this.contextForm.patchValue(context);
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      medications: this.api.searchMedications({}),
      queue: this.auth.isRoleAllowed(['INSTITUTION', 'ADMIN'])
        ? this.api.searchInstitutionDonations(context.institutionId, {})
        : of([]),
    }).subscribe({
      next: ({ medications, queue }) => {
        this.medications.set(medications);
        this.queue.set(queue);
        this.loading.set(false);
      },
      error: (error) => {
        this.error.set(readHttpError(error));
        this.loading.set(false);
      },
    });
  }
}
