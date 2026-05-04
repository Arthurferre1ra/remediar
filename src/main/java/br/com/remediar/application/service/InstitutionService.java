package br.com.remediar.application.service;

import br.com.remediar.domain.enums.InstitutionStatus;
import br.com.remediar.domain.model.Institution;
import br.com.remediar.domain.repository.InstitutionRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final GeoDistanceCalculator geoDistanceCalculator;

    public InstitutionService(
            InstitutionRepository institutionRepository,
            GeoDistanceCalculator geoDistanceCalculator
    ) {
        this.institutionRepository = institutionRepository;
        this.geoDistanceCalculator = geoDistanceCalculator;
    }

    @Transactional(readOnly = true)
    public List<InstitutionDistance> findNearby(BigDecimal donorLatitude, BigDecimal donorLongitude, BigDecimal radiusKm) {
        return institutionRepository.findByStatus(InstitutionStatus.ACTIVE).stream()
                .filter(Institution::canReceiveDonations)
                .map(institution -> new InstitutionDistance(
                        institution,
                        geoDistanceCalculator.distanceKm(
                                donorLatitude,
                                donorLongitude,
                                institution.getLatitude(),
                                institution.getLongitude()
                        )
                ))
                .filter(result -> result.distanceKm().compareTo(radiusKm) <= 0)
                .sorted(Comparator.comparing(InstitutionDistance::distanceKm))
                .toList();
    }

    public record InstitutionDistance(Institution institution, BigDecimal distanceKm) {
    }
}
