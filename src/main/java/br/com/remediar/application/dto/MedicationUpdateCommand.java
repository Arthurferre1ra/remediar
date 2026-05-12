package br.com.remediar.application.dto;

public record MedicationUpdateCommand(
        Integer quantityAvailable,
        String frontPhotoUrl,
        String blisterPhotoUrl
) {
}
