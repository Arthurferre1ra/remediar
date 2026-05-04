package br.com.remediar.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "medication_blacklist")
public class MedicationBlacklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer blockedTypeCode;

    @Column(length = 160)
    private String activeIngredient;

    @Column(length = 160)
    private String commercialName;

    @Column(nullable = false, length = 260)
    private String reason;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected MedicationBlacklistItem() {
    }

    public String getReason() {
        return reason;
    }
}
