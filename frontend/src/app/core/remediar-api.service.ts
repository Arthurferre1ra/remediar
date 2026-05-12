import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import {
  DonationMatchResponse,
  InstitutionNearbyResponse,
  MedicationResponse,
  OcrPreviewResponse,
} from './models';

type MedicationFilters = {
  name?: string;
  activeIngredient?: string;
  lotNumber?: string;
  expiresBefore?: string;
};

type InstitutionDonationFilters = {
  medicationName?: string;
  lotNumber?: string;
  deliveryDate?: string;
};

@Injectable({ providedIn: 'root' })
export class RemediarApiService {
  private readonly http = inject(HttpClient);

  previewOcr(rawOcrText: string) {
    return this.http.post<OcrPreviewResponse>('/api/v1/medications/ocr-preview', { rawOcrText });
  }

  searchMedications(filters: MedicationFilters) {
    return this.http.get<MedicationResponse[]>('/api/v1/medications', {
      params: this.buildParams(filters),
    });
  }

  createMedication(payload: object) {
    return this.http.post<MedicationResponse>('/api/v1/medications', payload);
  }

  updateMedication(id: number, payload: object) {
    return this.http.patch<MedicationResponse>(`/api/v1/medications/${id}`, payload);
  }

  cancelMedication(id: number) {
    return this.http.delete<void>(`/api/v1/medications/${id}`);
  }

  nearbyInstitutions(latitude: number, longitude: number, radiusKm?: number) {
    return this.http.get<InstitutionNearbyResponse[]>('/api/v1/institutions/nearby', {
      params: this.buildParams({
        latitude,
        longitude,
        radiusKm,
      }),
    });
  }

  createDonation(payload: object) {
    return this.http.post<DonationMatchResponse>('/api/v1/donations', payload);
  }

  changeDonationInstitution(id: number, payload: object) {
    return this.http.patch<DonationMatchResponse>(`/api/v1/donations/${id}/institution`, payload);
  }

  acceptDonation(id: number) {
    return this.http.post<DonationMatchResponse>(`/api/v1/donations/${id}/accept`, {});
  }

  confirmDonationDelivery(id: number, payload: object) {
    return this.http.post<DonationMatchResponse>(`/api/v1/donations/${id}/confirm-delivery`, payload);
  }

  cancelDonation(id: number) {
    return this.http.delete<void>(`/api/v1/donations/${id}`);
  }

  searchInstitutionDonations(institutionId: number, filters: InstitutionDonationFilters) {
    return this.http.get<DonationMatchResponse[]>(`/api/v1/donations/institution/${institutionId}`, {
      params: this.buildParams(filters),
    });
  }

  expireOverdueDonations() {
    return this.http.post<number>('/api/v1/donations/expire-overdue', {});
  }

  private buildParams(values: Record<string, unknown>): HttpParams {
    let params = new HttpParams();
    for (const [key, value] of Object.entries(values)) {
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, String(value));
      }
    }
    return params;
  }
}
