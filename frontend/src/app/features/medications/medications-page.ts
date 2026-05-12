import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AppContextService } from '../../core/app-context.service';
import { AuthService } from '../../core/auth.service';
import { readHttpError } from '../../core/http-error.util';
import { MedicationResponse, OcrPreviewResponse } from '../../core/models';
import { RemediarApiService } from '../../core/remediar-api.service';

@Component({
  selector: 'app-medications-page',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './medications-page.html',
  styleUrl: './medications-page.css',
})
export class MedicationsPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(RemediarApiService);

  protected readonly auth = inject(AuthService);
  protected readonly context = inject(AppContextService);

  protected readonly medications = signal<MedicationResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly pageMessage = signal<{ type: 'error' | 'success'; text: string } | null>(null);
  protected readonly ocrResult = signal<OcrPreviewResponse | null>(null);
  protected readonly editingId = signal<number | null>(null);

  protected readonly medicationTypeOptions = [
    { code: 1, label: 'Sem prescricao' },
    { code: 2, label: 'Com prescricao' },
    { code: 3, label: 'Antibiotico' },
    { code: 4, label: 'Controle especial' },
  ];

  protected readonly searchForm = this.fb.nonNullable.group({
    name: [''],
    activeIngredient: [''],
    lotNumber: [''],
    expiresBefore: [''],
  });

  protected readonly ocrForm = this.fb.nonNullable.group({
    rawOcrText: ['', Validators.required],
  });

  protected readonly medicationForm = this.fb.nonNullable.group({
    commercialName: ['', Validators.required],
    activeIngredient: ['', Validators.required],
    concentration: ['', Validators.required],
    manufacturer: ['', Validators.required],
    lotNumber: ['', Validators.required],
    expirationDate: ['', Validators.required],
    quantityAvailable: [1, [Validators.required, Validators.min(1)]],
    medicationTypeCode: [1, Validators.required],
    frontPhotoUrl: ['', Validators.required],
    blisterPhotoUrl: ['', Validators.required],
    storageDeclaration: [false, Validators.requiredTrue],
  });

  protected readonly editForm = this.fb.nonNullable.group({
    quantityAvailable: [1, [Validators.required, Validators.min(1)]],
    frontPhotoUrl: [''],
    blisterPhotoUrl: [''],
  });

  constructor() {
    this.search();
  }

  protected search(): void {
    this.loading.set(true);
    this.pageMessage.set(null);
    this.api.searchMedications(this.searchForm.getRawValue()).subscribe({
      next: (medications) => {
        this.medications.set(medications);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.pageMessage.set({ type: 'error', text: readHttpError(error) });
      },
    });
  }

  protected previewOcr(): void {
    if (this.ocrForm.invalid) {
      this.ocrForm.markAllAsTouched();
      return;
    }

    this.api.previewOcr(this.ocrForm.getRawValue().rawOcrText).subscribe({
      next: (result) => {
        this.ocrResult.set(result);
        if (result.lotNumber) {
          this.medicationForm.patchValue({ lotNumber: result.lotNumber });
        }
        if (result.expirationDate) {
          this.medicationForm.patchValue({ expirationDate: result.expirationDate });
        }
      },
      error: (error) => {
        this.pageMessage.set({ type: 'error', text: readHttpError(error) });
      },
    });
  }

  protected createMedication(): void {
    if (this.medicationForm.invalid) {
      this.medicationForm.markAllAsTouched();
      return;
    }

    const context = this.context.context();
    const payload = {
      donorId: context.donorId,
      donorCpf: context.donorCpf,
      ...this.medicationForm.getRawValue(),
    };

    this.api.createMedication(payload).subscribe({
      next: () => {
        this.pageMessage.set({ type: 'success', text: 'Medicamento cadastrado com sucesso.' });
        this.medicationForm.reset({
          commercialName: '',
          activeIngredient: '',
          concentration: '',
          manufacturer: '',
          lotNumber: '',
          expirationDate: '',
          quantityAvailable: 1,
          medicationTypeCode: 1,
          frontPhotoUrl: '',
          blisterPhotoUrl: '',
          storageDeclaration: false,
        });
        this.search();
      },
      error: (error) => {
        this.pageMessage.set({ type: 'error', text: readHttpError(error) });
      },
    });
  }

  protected beginEdit(medication: MedicationResponse): void {
    this.editingId.set(medication.id);
    this.editForm.setValue({
      quantityAvailable: medication.quantityAvailable,
      frontPhotoUrl: medication.frontPhotoUrl,
      blisterPhotoUrl: medication.blisterPhotoUrl,
    });
  }

  protected saveEdit(): void {
    if (this.editForm.invalid || this.editingId() === null) {
      this.editForm.markAllAsTouched();
      return;
    }

    this.api.updateMedication(this.editingId()!, this.editForm.getRawValue()).subscribe({
      next: () => {
        this.pageMessage.set({ type: 'success', text: 'Medicamento atualizado.' });
        this.editingId.set(null);
        this.search();
      },
      error: (error) => {
        this.pageMessage.set({ type: 'error', text: readHttpError(error) });
      },
    });
  }

  protected cancelEdit(): void {
    this.editingId.set(null);
  }

  protected cancelMedication(id: number): void {
    this.api.cancelMedication(id).subscribe({
      next: () => {
        this.pageMessage.set({ type: 'success', text: 'Medicamento cancelado logicamente.' });
        this.search();
      },
      error: (error) => {
        this.pageMessage.set({ type: 'error', text: readHttpError(error) });
      },
    });
  }

  protected canMutateMedications(): boolean {
    return this.auth.isRoleAllowed(['DONOR', 'ADMIN']);
  }
}
