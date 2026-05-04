package br.com.remediar.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String aggregateType;

    @Column(nullable = false)
    private Long aggregateId;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(nullable = false, length = 18)
    private String actorDocument;

    @Column(nullable = false, length = 1000)
    private String details;

    @Column(nullable = false)
    private Instant occurredAt;

    protected AuditEvent() {
    }

    public AuditEvent(String aggregateType, Long aggregateId, String action, String actorDocument, String details) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.action = action;
        this.actorDocument = actorDocument;
        this.details = details;
        this.occurredAt = Instant.now();
    }

    public Long getId() {
        return id;
    }
}
