package br.com.remediar.web.controller;

import br.com.remediar.application.service.InstitutionService;
import br.com.remediar.web.dto.InstitutionNearbyResponse;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/institutions")
public class InstitutionController {

    private final InstitutionService institutionService;
    private final BigDecimal defaultRadiusKm;

    public InstitutionController(
            InstitutionService institutionService,
            @Value("${remediar.match.default-radius-km}") BigDecimal defaultRadiusKm
    ) {
        this.institutionService = institutionService;
        this.defaultRadiusKm = defaultRadiusKm;
    }

    @GetMapping("/nearby")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public List<InstitutionNearbyResponse> nearby(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
            @RequestParam(required = false) @Positive BigDecimal radiusKm
    ) {
        BigDecimal radius = radiusKm == null ? defaultRadiusKm : radiusKm;
        return institutionService.findNearby(latitude, longitude, radius)
                .stream()
                .map(InstitutionNearbyResponse::from)
                .toList();
    }
}
