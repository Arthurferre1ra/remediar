package br.com.remediar.web.dto;

import br.com.remediar.application.dto.MedicationCreateCommand;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record MedicationCreateRequest(
        @NotNull Long donorId,
        @NotBlank @Pattern(regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}") String donorCpf,
        @NotBlank String commercialName,
        @NotBlank String activeIngredient,
        @NotBlank String concentration,
        @NotBlank String manufacturer,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9.-]{3,60}") String lotNumber,
        @NotNull @Future LocalDate expirationDate,
        @NotNull @Positive Integer quantityAvailable,
        @NotNull Integer medicationTypeCode,
        @NotBlank String frontPhotoUrl,
        @NotBlank String blisterPhotoUrl,
        boolean storageDeclaration
) {
    public MedicationCreateCommand toCommand() {
        return new MedicationCreateCommand(
                donorId,
                donorCpf,
                commercialName,
                activeIngredient,
                concentration,
                manufacturer,
                lotNumber,
                expirationDate,
                quantityAvailable,
                medicationTypeCode,
                frontPhotoUrl,
                blisterPhotoUrl,
                storageDeclaration
        );
    }
}
