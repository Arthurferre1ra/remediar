package br.com.remediar.application.service;

import br.com.remediar.domain.model.AuditEvent;
import br.com.remediar.domain.repository.AuditEventRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    public void record(String aggregateType, Long aggregateId, String action, String actorDocument, String details) {
        repository.save(new AuditEvent(aggregateType, aggregateId, action, actorDocument, details));
    }
}
