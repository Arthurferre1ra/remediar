package br.com.remediar.web.dto;

import br.com.remediar.domain.model.Medication;
import java.time.Instant;
import java.time.LocalDate;

public record MedicationResponse(
        Long id,
        Long donorId,
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
        Integer statusCode,
        String status,
        Instant createdAt
) {
    public static MedicationResponse from(Medication medication) {
        return new MedicationResponse(
                medication.getId(),
                medication.getDonorId(),
                medication.getCommercialName(),
                medication.getActiveIngredient(),
                medication.getConcentration(),
                medication.getManufacturer(),
                medication.getLotNumber(),
                medication.getExpirationDate(),
                medication.getQuantityAvailable(),
                medication.getMedicationTypeCode(),
                medication.getFrontPhotoUrl(),
                medication.getBlisterPhotoUrl(),
                medication.getStatus().getCode(),
                medication.getStatus().name(),
                medication.getCreatedAt()
        );
    }
}
