package br.com.remediar.domain.model;

import br.com.remediar.common.BusinessException;
import br.com.remediar.domain.enums.MedicationStatus;
import br.com.remediar.domain.enums.MedicationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "medications")
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long donorId;

    @Column(nullable = false, length = 14)
    private String donorCpf;

    @Column(nullable = false, length = 160)
    private String commercialName;

    @Column(nullable = false, length = 160)
    private String activeIngredient;

    @Column(nullable = false, length = 80)
    private String concentration;

    @Column(nullable = false, length = 160)
    private String manufacturer;

    @Column(nullable = false, length = 60)
    private String lotNumber;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private Integer quantityAvailable;

    @Column(nullable = false)
    private Integer medicationTypeCode;

    @Column(nullable = false, length = 500)
    private String frontPhotoUrl;

    @Column(nullable = false, length = 500)
    private String blisterPhotoUrl;

    @Column(nullable = false)
    private boolean storageDeclaration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MedicationStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Medication() {
    }

    public Medication(
            Long donorId,
            String donorCpf,
            String commercialName,
            String activeIngredient,
            String concentration,
            String manufacturer,
            String lotNumber,
            LocalDate expirationDate,
            Integer quantityAvailable,
            MedicationType medicationType,
            String frontPhotoUrl,
            String blisterPhotoUrl,
            boolean storageDeclaration
    ) {
        this.donorId = donorId;
        this.donorCpf = donorCpf;
        this.commercialName = commercialName;
        this.activeIngredient = activeIngredient;
        this.concentration = concentration;
        this.manufacturer = manufacturer;
        this.lotNumber = lotNumber;
        this.expirationDate = expirationDate;
        this.quantityAvailable = quantityAvailable;
        this.medicationTypeCode = medicationType.getCode();
        this.frontPhotoUrl = frontPhotoUrl;
        this.blisterPhotoUrl = blisterPhotoUrl;
        this.storageDeclaration = storageDeclaration;
        this.status = MedicationStatus.AVAILABLE;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void updateAvailableData(Integer quantityAvailable, String frontPhotoUrl, String blisterPhotoUrl) {
        ensureAvailable();
        if (quantityAvailable != null) {
            if (quantityAvailable <= 0) {
                throw new BusinessException("Quantidade deve ser maior que zero.");
            }
            this.quantityAvailable = quantityAvailable;
        }
        if (frontPhotoUrl != null && !frontPhotoUrl.isBlank()) {
            this.frontPhotoUrl = frontPhotoUrl;
        }
        if (blisterPhotoUrl != null && !blisterPhotoUrl.isBlank()) {
            this.blisterPhotoUrl = blisterPhotoUrl;
        }
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (status == MedicationStatus.DELIVERED) {
            throw new BusinessException("Medicamento entregue nao pode ser cancelado.");
        }
        this.status = MedicationStatus.EXPIRED_OR_CANCELED;
        this.updatedAt = Instant.now();
    }

    public void markDonationInProgress() {
        ensureAvailable();
        this.status = MedicationStatus.DONATION_IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    public void markDelivered() {
        if (status != MedicationStatus.DONATION_IN_PROGRESS) {
            throw new BusinessException("Medicamento nao esta em processo de doacao.");
        }
        this.status = MedicationStatus.DELIVERED;
        this.updatedAt = Instant.now();
    }

    public void releaseToAvailable() {
        if (status == MedicationStatus.DELIVERED) {
            throw new BusinessException("Medicamento entregue nao pode voltar ao estoque disponivel.");
        }
        this.status = MedicationStatus.AVAILABLE;
        this.updatedAt = Instant.now();
    }

    public void ensureAvailable() {
        if (status != MedicationStatus.AVAILABLE) {
            throw new BusinessException("Operacao permitida apenas para medicamentos disponiveis.");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getDonorId() {
        return donorId;
    }

    public String getDonorCpf() {
        return donorCpf;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public String getActiveIngredient() {
        return activeIngredient;
    }

    public String getConcentration() {
        return concentration;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }

    public Integer getMedicationTypeCode() {
        return medicationTypeCode;
    }

    public String getFrontPhotoUrl() {
        return frontPhotoUrl;
    }

    public String getBlisterPhotoUrl() {
        return blisterPhotoUrl;
    }

    public boolean isStorageDeclaration() {
        return storageDeclaration;
    }

    public MedicationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
