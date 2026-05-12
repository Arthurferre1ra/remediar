import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AppContextService } from '../../core/app-context.service';
import { AuthService } from '../../core/auth.service';
import { readHttpError } from '../../core/http-error.util';
import { DonationMatchResponse, InstitutionNearbyResponse, MedicationResponse } from '../../core/models';
import { RemediarApiService } from '../../core/remediar-api.service';

const RECENT_MATCHES_KEY = 'remediar-recent-matches';

@Component({
  selector: 'app-matches-page',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './matches-page.html',
  styleUrl: './matches-page.css',
})
export class MatchesPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(RemediarApiService);

  protected readonly auth = inject(AuthService);
  protected readonly context = inject(AppContextService);

  protected readonly availableMedications = signal<MedicationResponse[]>([]);
  protected readonly nearbyInstitutions = signal<InstitutionNearbyResponse[]>([]);
  protected readonly recentMatches = signal<DonationMatchResponse[]>(this.loadRecentMatches());
  protected readonly message = signal<{ type: 'error' | 'success'; text: string } | null>(null);

  protected readonly nearbyForm = this.fb.nonNullable.group({
    latitude: [this.context.context().donorLatitude, Validators.required],
    longitude: [this.context.context().donorLongitude, Validators.required],
    radiusKm: [this.context.context().radiusKm, [Validators.required, Validators.min(1)]],
  });

  protected readonly createForm = this.fb.nonNullable.group({
    medicationId: [0, [Validators.required, Validators.min(1)]],
    institutionId: [this.context.context().institutionId, [Validators.required, Validators.min(1)]],
  });

  protected readonly changeForm = this.fb.nonNullable.group({
    donationId: [0, [Validators.required, Validators.min(1)]],
    institutionId: [this.context.context().institutionId, [Validators.required, Validators.min(1)]],
    donorLatitude: [this.context.context().donorLatitude, Validators.required],
    donorLongitude: [this.context.context().donorLongitude, Validators.required],
  });

  protected readonly cancelForm = this.fb.nonNullable.group({
    donationId: [0, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    this.loadAvailableMedications();
  }

  protected loadAvailableMedications(): void {
    this.api.searchMedications({}).subscribe({
      next: (medications) => {
        this.availableMedications.set(medications.filter((item) => item.status === 'AVAILABLE'));
      },
      error: (error) => {
        this.message.set({ type: 'error', text: readHttpError(error) });
      },
    });
  }

  protected searchNearby(): void {
    const payload = this.nearbyForm.getRawValue();
    this.api.nearbyInstitutions(payload.latitude, payload.longitude, payload.radiusKm).subscribe({
      next: (institutions) => {
        this.nearbyInstitutions.set(institutions);
      },
      error: (error) => {
        this.message.set({ type: 'error', text: readHttpError(error) });
      },
    });
  }

  protected selectInstitution(institution: InstitutionNearbyResponse): void {
    this.createForm.patchValue({ institutionId: institution.id });
    this.changeForm.patchValue({ institutionId: institution.id });
  }

  protected createDonation(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const context = this.context.context();
    const payload = {
      medicationId: this.createForm.getRawValue().medicationId,
      donorId: context.donorId,
      institutionId: this.createForm.getRawValue().institutionId,
      donorLatitude: context.donorLatitude,
      donorLongitude: context.donorLongitude,
      radiusKm: context.radiusKm,
    };

    this.api.createDonation(payload).subscribe({
      next: (match) => {
        this.pushRecentMatch(match);
        this.message.set({ type: 'success', text: `Match #${match.id} criado com sucesso.` });
        this.changeForm.patchValue({ donationId: match.id });
        this.cancelForm.patchValue({ donationId: match.id });
        this.loadAvailableMedications();
      },
      error: (error) => {
        this.message.set({ type: 'error', text: readHttpError(error) });
      },
    });
  }

  protected changeInstitution(): void {
    if (this.changeForm.invalid) {
      this.changeForm.markAllAsTouched();
      return;
    }

    const { donationId, ...payload } = this.changeForm.getRawValue();
    this.api.changeDonationInstitution(donationId, payload).subscribe({
      next: (match) => {
        this.pushRecentMatch(match);
        this.message.set({ type: 'success', text: `Match #${match.id} redirecionado para outra ONG.` });
      },
      error: (error) => {
        this.message.set({ type: 'error', text: readHttpError(error) });
      },
    });
  }

  protected cancelDonation(): void {
    if (this.cancelForm.invalid) {
      this.cancelForm.markAllAsTouched();
      return;
    }

    const donationId = this.cancelForm.getRawValue().donationId;
    this.api.cancelDonation(donationId).subscribe({
      next: () => {
        this.message.set({ type: 'success', text: `Match #${donationId} cancelado.` });
        this.recentMatches.update((current) =>
          current.map((item) => item.id === donationId ? { ...item, status: 'EXPIRED', statusCode: 4 } : item),
        );
        localStorage.setItem(RECENT_MATCHES_KEY, JSON.stringify(this.recentMatches()));
        this.loadAvailableMedications();
      },
      error: (error) => {
        this.message.set({ type: 'error', text: readHttpError(error) });
      },
    });
  }

  private pushRecentMatch(match: DonationMatchResponse): void {
    this.recentMatches.update((current) => {
      const next = [match, ...current.filter((item) => item.id !== match.id)].slice(0, 8);
      localStorage.setItem(RECENT_MATCHES_KEY, JSON.stringify(next));
      return next;
    });
  }

  private loadRecentMatches(): DonationMatchResponse[] {
    const raw = localStorage.getItem(RECENT_MATCHES_KEY);
    if (!raw) {
      return [];
    }

    try {
      return JSON.parse(raw) as DonationMatchResponse[];
    } catch {
      return [];
    }
  }
}
