package br.com.remediar.web.dto;

import jakarta.validation.constraints.Positive;

public record MedicationUpdateRequest(
        @Positive Integer quantityAvailable,
        String frontPhotoUrl,
        String blisterPhotoUrl
) {
}
