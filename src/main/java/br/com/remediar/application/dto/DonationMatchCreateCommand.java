package br.com.remediar.application.dto;

import java.math.BigDecimal;

public record DonationMatchCreateCommand(
        Long medicationId,
        Long donorId,
        Long institutionId,
        BigDecimal donorLatitude,
        BigDecimal donorLongitude,
        BigDecimal radiusKm
) {
}
