package br.com.remediar.web.dto;

import br.com.remediar.application.dto.DonationDeliveryConfirmationCommand;
import jakarta.validation.constraints.NotBlank;

public record DonationDeliveryConfirmationRequest(
        @NotBlank String validationCode
) {
    public DonationDeliveryConfirmationCommand toCommand(String actorDocument) {
        return new DonationDeliveryConfirmationCommand(validationCode, actorDocument);
    }
}
