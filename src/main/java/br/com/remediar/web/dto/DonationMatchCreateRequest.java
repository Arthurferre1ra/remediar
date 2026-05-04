package br.com.remediar.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record DonationMatchCreateRequest(
        @NotNull Long medicationId,
        @NotNull Long donorId,
        @NotNull Long institutionId,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal donorLatitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal donorLongitude,
        @Positive BigDecimal radiusKm
) {
}
