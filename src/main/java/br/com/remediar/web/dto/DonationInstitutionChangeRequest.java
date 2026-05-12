package br.com.remediar.web.dto;

import br.com.remediar.application.dto.DonationInstitutionChangeCommand;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DonationInstitutionChangeRequest(
        @NotNull Long institutionId,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal donorLatitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal donorLongitude
) {
    public DonationInstitutionChangeCommand toCommand(String actorDocument) {
        return new DonationInstitutionChangeCommand(institutionId, donorLatitude, donorLongitude, actorDocument);
    }
}
