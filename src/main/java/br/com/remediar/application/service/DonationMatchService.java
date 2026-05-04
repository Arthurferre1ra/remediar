package br.com.remediar.application.service;

import br.com.remediar.application.ports.ValidationCodeGenerator;
import br.com.remediar.common.BusinessException;
import br.com.remediar.common.NotFoundException;
import br.com.remediar.domain.enums.DonationFlowStatus;
import br.com.remediar.domain.model.DonationMatch;
import br.com.remediar.domain.model.Institution;
import br.com.remediar.domain.model.Medication;
import br.com.remediar.domain.repository.DonationMatchRepository;
import br.com.remediar.domain.repository.InstitutionRepository;
import br.com.remediar.domain.repository.MedicationRepository;
import br.com.remediar.web.dto.DonationMatchCreateRequest;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DonationMatchService {

    private final DonationMatchRepository donationMatchRepository;
    private final MedicationRepository medicationRepository;
    private final InstitutionRepository institutionRepository;
    private final GeoDistanceCalculator geoDistanceCalculator;
    private final BusinessDayCalculator businessDayCalculator;
    private final ValidationCodeGenerator validationCodeGenerator;
    private final AuditService auditService;
    private final Clock clock;
    private final BigDecimal defaultRadiusKm;
    private final int deliveryBusinessDays;

    public DonationMatchService(
            DonationMatchRepository donationMatchRepository,
            MedicationRepository medicationRepository,
            InstitutionRepository institutionRepository,
            GeoDistanceCalculator geoDistanceCalculator,
            BusinessDayCalculator businessDayCalculator,
            ValidationCodeGenerator validationCodeGenerator,
            AuditService auditService,
            Clock clock,
            @Value("${remediar.match.default-radius-km}") BigDecimal defaultRadiusKm,
            @Value("${remediar.donation.delivery-business-days}") int deliveryBusinessDays
    ) {
        this.donationMatchRepository = donationMatchRepository;
        this.medicationRepository = medicationRepository;
        this.institutionRepository = institutionRepository;
        this.geoDistanceCalculator = geoDistanceCalculator;
        this.businessDayCalculator = businessDayCalculator;
        this.validationCodeGenerator = validationCodeGenerator;
        this.auditService = auditService;
        this.clock = clock;
        this.defaultRadiusKm = defaultRadiusKm;
        this.deliveryBusinessDays = deliveryBusinessDays;
    }

    @Transactional
    public DonationMatch create(DonationMatchCreateRequest request) {
        Medication medication = medicationRepository.findById(request.medicationId())
                .orElseThrow(() -> new NotFoundException("Medicamento nao encontrado."));
        medication.ensureAvailable();

        if (!medication.getDonorId().equals(request.donorId())) {
            throw new BusinessException("Medicamento nao pertence ao doador informado.");
        }
        if (donationMatchRepository.existsByMedicationIdAndStatusIn(
                medication.getId(),
                List.of(DonationFlowStatus.AWAITING_ACCEPTANCE, DonationFlowStatus.AWAITING_DELIVERY)
        )) {
            throw new BusinessException("Medicamento ja possui match em aberto.");
        }

        Institution institution = findEligibleInstitution(request.institutionId());
        BigDecimal radiusKm = request.radiusKm() == null ? defaultRadiusKm : request.radiusKm();
        BigDecimal distanceKm = distanceFromDonor(request.donorLatitude(), request.donorLongitude(), institution);
        ensureInsideRadius(distanceKm, radiusKm);

        DonationMatch donationMatch = donationMatchRepository.save(new DonationMatch(
                medication,
                request.donorId(),
                institution,
                radiusKm,
                request.donorLatitude(),
                request.donorLongitude(),
                distanceKm
        ));

        auditService.record(
                "DonationMatch",
                donationMatch.getId(),
                "MATCH_CREATED",
                medication.getDonorCpf(),
                "Match criado para ONG " + institution.getId() + " a " + distanceKm + "km."
        );
        return donationMatch;
    }

    @Transactional
    public DonationMatch changeInstitution(Long donationId, Long institutionId, BigDecimal donorLatitude, BigDecimal donorLongitude, String actorDocument) {
        DonationMatch donationMatch = findDetailed(donationId);
        Institution institution = findEligibleInstitution(institutionId);
        BigDecimal distanceKm = distanceFromDonor(donorLatitude, donorLongitude, institution);
        ensureInsideRadius(distanceKm, donationMatch.getRadiusKm());
        donationMatch.changeInstitution(institution, distanceKm);
        auditService.record("DonationMatch", donationId, "INSTITUTION_CHANGED", actorDocument, "ONG alterada antes do aceite.");
        return donationMatch;
    }

    @Transactional
    public DonationMatch accept(Long donationId, String actorDocument) {
        DonationMatch donationMatch = findDetailed(donationId);
        String validationCode = validationCodeGenerator.generate(donationId);
        LocalDate deadline = businessDayCalculator.addBusinessDays(LocalDate.now(clock), deliveryBusinessDays);
        donationMatch.accept(validationCode, deadline);
        auditService.record("DonationMatch", donationId, "ACCEPTED", actorDocument, "ONG aceitou a doacao e QR/codigo foi gerado.");
        return donationMatch;
    }

    @Transactional
    public DonationMatch confirmDelivery(Long donationId, String validationCode, String actorDocument) {
        DonationMatch donationMatch = findDetailed(donationId);
        donationMatch.complete(validationCode);
        auditService.record("DonationMatch", donationId, "DELIVERY_CONFIRMED", actorDocument, "Entrega fisica confirmada por QR/codigo.");
        return donationMatch;
    }

    @Transactional
    public void cancelOrExpire(Long donationId, String actorDocument) {
        DonationMatch donationMatch = findDetailed(donationId);
        donationMatch.expireAndReleaseMedication();
        auditService.record("DonationMatch", donationId, "EXPIRED_OR_CANCELED", actorDocument, "Fluxo cancelado e item liberado.");
    }

    @Transactional
    public int expireOverdueMatches() {
        List<DonationMatch> overdue = donationMatchRepository.findByStatusAndDeliveryDeadlineBefore(
                DonationFlowStatus.AWAITING_DELIVERY,
                LocalDate.now(clock)
        );
        overdue.forEach(DonationMatch::expireAndReleaseMedication);
        return overdue.size();
    }

    @Transactional(readOnly = true)
    public List<DonationMatch> searchForInstitution(Long institutionId, String medicationName, String lotNumber, LocalDate deliveryDate) {
        return donationMatchRepository.searchForInstitution(
                institutionId,
                blankToNull(medicationName),
                blankToNull(lotNumber),
                deliveryDate
        );
    }

    private DonationMatch findDetailed(Long donationId) {
        return donationMatchRepository.findDetailedById(donationId)
                .orElseThrow(() -> new NotFoundException("Doacao nao encontrada."));
    }

    private Institution findEligibleInstitution(Long institutionId) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new NotFoundException("ONG nao encontrada."));
        if (!institution.canReceiveDonations()) {
            throw new BusinessException("ONG precisa estar ativa e possuir farmaceutico responsavel.");
        }
        return institution;
    }

    private BigDecimal distanceFromDonor(BigDecimal donorLatitude, BigDecimal donorLongitude, Institution institution) {
        return geoDistanceCalculator.distanceKm(donorLatitude, donorLongitude, institution.getLatitude(), institution.getLongitude());
    }

    private void ensureInsideRadius(BigDecimal distanceKm, BigDecimal radiusKm) {
        if (distanceKm.compareTo(radiusKm) > 0) {
            throw new BusinessException("ONG fora do raio permitido de " + radiusKm + "km.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
