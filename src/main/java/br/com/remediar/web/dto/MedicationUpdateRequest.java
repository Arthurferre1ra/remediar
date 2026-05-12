package br.com.remediar.web.dto;

import br.com.remediar.application.dto.MedicationUpdateCommand;
import jakarta.validation.constraints.Positive;

public record MedicationUpdateRequest(
        @Positive Integer quantityAvailable,
        String frontPhotoUrl,
        String blisterPhotoUrl
) {
    public MedicationUpdateCommand toCommand() {
        return new MedicationUpdateCommand(quantityAvailable, frontPhotoUrl, blisterPhotoUrl);
    }
}
