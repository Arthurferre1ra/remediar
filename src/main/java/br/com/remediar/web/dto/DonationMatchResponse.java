package br.com.remediar.web.dto;

import br.com.remediar.domain.model.DonationMatch;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DonationMatchResponse(
        Long id,
        Long medicationId,
        Long donorId,
        Long institutionId,
        BigDecimal radiusKm,
        BigDecimal distanceKm,
        String validationCode,
        Integer statusCode,
        String status,
        LocalDate deliveryDeadline
) {
    public static DonationMatchResponse from(DonationMatch donationMatch) {
        return new DonationMatchResponse(
                donationMatch.getId(),
                donationMatch.getMedication().getId(),
                donationMatch.getDonorId(),
                donationMatch.getInstitution().getId(),
                donationMatch.getRadiusKm(),
                donationMatch.getDistanceKm(),
                donationMatch.getValidationCode(),
                donationMatch.getStatus().getCode(),
                donationMatch.getStatus().name(),
                donationMatch.getDeliveryDeadline()
        );
    }
}
