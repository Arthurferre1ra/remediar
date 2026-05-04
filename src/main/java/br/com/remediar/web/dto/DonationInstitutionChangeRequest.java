package br.com.remediar.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DonationInstitutionChangeRequest(
        @NotNull Long institutionId,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal donorLatitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal donorLongitude,
        @NotBlank String actorDocument
) {
}
