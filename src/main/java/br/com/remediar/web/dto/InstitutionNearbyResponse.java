package br.com.remediar.web.dto;

import br.com.remediar.application.service.InstitutionService;
import java.math.BigDecimal;

public record InstitutionNearbyResponse(
        Long id,
        String legalName,
        String cnpj,
        String pharmacistName,
        BigDecimal distanceKm
) {
    public static InstitutionNearbyResponse from(InstitutionService.InstitutionDistance result) {
        return new InstitutionNearbyResponse(
                result.institution().getId(),
                result.institution().getLegalName(),
                result.institution().getCnpj(),
                result.institution().getPharmacistName(),
                result.distanceKm()
        );
    }
}
