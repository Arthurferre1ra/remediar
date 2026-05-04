package br.com.remediar.web.dto;

import jakarta.validation.constraints.NotBlank;

public record DonationDeliveryConfirmationRequest(
        @NotBlank String validationCode,
        @NotBlank String actorDocument
) {
}
