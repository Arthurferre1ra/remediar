package br.com.remediar.application.dto;

import java.math.BigDecimal;

public record DonationInstitutionChangeCommand(
        Long institutionId,
        BigDecimal donorLatitude,
        BigDecimal donorLongitude,
        String actorDocument
) {
}
