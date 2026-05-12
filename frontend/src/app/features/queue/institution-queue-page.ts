import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AppContextService } from '../../core/app-context.service';
import { AuthService } from '../../core/auth.service';
import { readHttpError } from '../../core/http-error.util';
import { DonationMatchResponse } from '../../core/models';
import { RemediarApiService } from '../../core/remediar-api.service';

@Component({
  selector: 'app-institution-queue-page',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './institution-queue-page.html',
  styleUrl: './institution-queue-page.css',
})
export class InstitutionQueuePageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(RemediarApiService);

  protected readonly auth = inject(AuthService);
  protected readonly context = inject(AppContextService);

  protected readonly queue = signal<DonationMatchResponse[]>([]);
  protected readonly selectedDeliveryId = signal<number | null>(null);
  protected readonly message = signal<{ type: 'error' | 'success'; text: string } | null>(null);
  protected readonly loading = signal(false);

  protected readonly searchForm = this.fb.nonNullable.group({
    institutionId: [this.context.context().institutionId, [Validators.required, Validators.min(1)]],
    medicationName: [''],
    lotNumber: [''],
    deliveryDate: [''],
  });

  protected readonly deliveryForm = this.fb.nonNullable.group({
    validationCode: ['', Validators.required],
  });

  constructor() {
    this.search();
  }

  protected search(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    const { institutionId, ...filters } = this.searchForm.getRawValue();
    this.api.searchInstitutionDonations(institutionId, filters).subscribe({
      next: (queue) => {
        this.queue.set(queue);
        this.loading.set(false);
      },
      error: (error) => {
        this.message.set({ type: 'error', text: readHttpError(error) });
        this.loading.set(false);
      },
    });
  }

  protected acceptDonation(id: number): void {
    this.api.acceptDonation(id).subscribe({
      next: (response) => {
        this.message.set({
          type: 'success',
          text: `Doacao #${id} aceita. Codigo gerado: ${response.validationCode}.`,
        });
        this.selectedDeliveryId.set(id);
        this.deliveryForm.patchValue({ validationCode: response.validationCode ?? '' });
        this.search();
      },
      error: (error) => {
        this.message.set({ type: 'error', text: readHttpError(error) });
      },
    });
  }

  protected openDelivery(match: DonationMatchResponse): void {
    this.selectedDeliveryId.set(match.id);
    this.deliveryForm.patchValue({ validationCode: match.validationCode ?? '' });
  }

  protected confirmDelivery(): void {
    if (this.deliveryForm.invalid || this.selectedDeliveryId() === null) {
      this.deliveryForm.markAllAsTouched();
      return;
    }

    this.api.confirmDonationDelivery(this.selectedDeliveryId()!, this.deliveryForm.getRawValue()).subscribe({
      next: (response) => {
        this.message.set({ type: 'success', text: `Doacao #${response.id} marcada como concluida.` });
        this.selectedDeliveryId.set(null);
        this.deliveryForm.reset({
          validationCode: '',
        });
        this.search();
      },
      error: (error) => {
        this.message.set({ type: 'error', text: readHttpError(error) });
      },
    });
  }
}
