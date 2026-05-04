package br.com.remediar.domain.model;

import br.com.remediar.common.BusinessException;
import br.com.remediar.domain.enums.DonationFlowStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "donation_matches")
public class DonationMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(nullable = false)
    private Long donorId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal radiusKm;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal donorLatitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal donorLongitude;

    @Column(nullable = false, precision = 8, scale = 3)
    private BigDecimal distanceKm;

    @Column(nullable = false)
    private Instant matchedAt;

    private Instant acceptedAt;

    private Instant completedAt;

    @Column(length = 120)
    private String validationCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DonationFlowStatus status;

    private LocalDate deliveryDeadline;

    protected DonationMatch() {
    }

    public DonationMatch(
            Medication medication,
            Long donorId,
            Institution institution,
            BigDecimal radiusKm,
            BigDecimal donorLatitude,
            BigDecimal donorLongitude,
            BigDecimal distanceKm
    ) {
        this.medication = medication;
        this.donorId = donorId;
        this.institution = institution;
        this.radiusKm = radiusKm;
        this.donorLatitude = donorLatitude;
        this.donorLongitude = donorLongitude;
        this.distanceKm = distanceKm;
        this.matchedAt = Instant.now();
        this.status = DonationFlowStatus.AWAITING_ACCEPTANCE;
    }

    public void changeInstitution(Institution institution, BigDecimal distanceKm) {
        ensureAwaitingAcceptance();
        this.institution = institution;
        this.distanceKm = distanceKm;
    }

    public void accept(String validationCode, LocalDate deliveryDeadline) {
        ensureAwaitingAcceptance();
        this.validationCode = validationCode;
        this.deliveryDeadline = deliveryDeadline;
        this.acceptedAt = Instant.now();
        this.status = DonationFlowStatus.AWAITING_DELIVERY;
        this.medication.markDonationInProgress();
    }

    public void complete(String validationCode) {
        if (status != DonationFlowStatus.AWAITING_DELIVERY) {
            throw new BusinessException("Doacao nao esta aguardando entrega.");
        }
        if (this.validationCode == null || !this.validationCode.equals(validationCode)) {
            throw new BusinessException("Codigo de validacao invalido.");
        }
        this.completedAt = Instant.now();
        this.status = DonationFlowStatus.COMPLETED;
        this.medication.markDelivered();
    }

    public void expireAndReleaseMedication() {
        if (status == DonationFlowStatus.COMPLETED) {
            throw new BusinessException("Doacao concluida nao pode ser expirada.");
        }
        this.status = DonationFlowStatus.EXPIRED;
        this.medication.releaseToAvailable();
    }

    private void ensureAwaitingAcceptance() {
        if (status != DonationFlowStatus.AWAITING_ACCEPTANCE) {
            throw new BusinessException("Alteracao permitida apenas enquanto aguarda aceite.");
        }
    }

    public Long getId() {
        return id;
    }

    public Medication getMedication() {
        return medication;
    }

    public Long getDonorId() {
        return donorId;
    }

    public Institution getInstitution() {
        return institution;
    }

    public BigDecimal getRadiusKm() {
        return radiusKm;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public Instant getMatchedAt() {
        return matchedAt;
    }

    public String getValidationCode() {
        return validationCode;
    }

    public DonationFlowStatus getStatus() {
        return status;
    }

    public LocalDate getDeliveryDeadline() {
        return deliveryDeadline;
    }
}
