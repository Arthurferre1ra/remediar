package br.com.remediar.application.dto;

public record DonationDeliveryConfirmationCommand(
        String validationCode,
        String actorDocument
) {
}
