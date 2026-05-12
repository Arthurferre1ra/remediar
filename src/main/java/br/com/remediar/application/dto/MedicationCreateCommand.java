package br.com.remediar.application.dto;

import java.time.LocalDate;

public record MedicationCreateCommand(
        Long donorId,
        String donorCpf,
        String commercialName,
        String activeIngredient,
        String concentration,
        String manufacturer,
        String lotNumber,
        LocalDate expirationDate,
        Integer quantityAvailable,
        Integer medicationTypeCode,
        String frontPhotoUrl,
        String blisterPhotoUrl,
        boolean storageDeclaration
) {
}
