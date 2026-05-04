package br.com.remediar.domain.model;

import br.com.remediar.domain.enums.InstitutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "institutions")
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String legalName;

    @Column(nullable = false, length = 18, unique = true)
    private String cnpj;

    @Column(nullable = false, length = 160)
    private String pharmacistName;

    @Column(nullable = false, length = 40)
    private String pharmacistRegistry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InstitutionStatus status;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Institution() {
    }

    public boolean canReceiveDonations() {
        return status == InstitutionStatus.ACTIVE
                && pharmacistName != null
                && !pharmacistName.isBlank()
                && pharmacistRegistry != null
                && !pharmacistRegistry.isBlank();
    }

    public Long getId() {
        return id;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getPharmacistName() {
        return pharmacistName;
    }

    public InstitutionStatus getStatus() {
        return status;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }
}
