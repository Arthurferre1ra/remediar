package br.com.remediar.application.service;

import br.com.remediar.application.ports.OcrMedicationDataExtractor;
import br.com.remediar.common.BusinessException;
import br.com.remediar.common.NotFoundException;
import br.com.remediar.domain.enums.MedicationType;
import br.com.remediar.domain.model.Medication;
import br.com.remediar.domain.model.MedicationBlacklistItem;
import br.com.remediar.domain.repository.MedicationBlacklistRepository;
import br.com.remediar.domain.repository.MedicationRepository;
import br.com.remediar.web.dto.MedicationCreateRequest;
import br.com.remediar.web.dto.MedicationUpdateRequest;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicationService {

    private static final int MINIMUM_VALIDITY_DAYS = 45;

    private final MedicationRepository medicationRepository;
    private final MedicationBlacklistRepository blacklistRepository;
    private final AuditService auditService;
    private final OcrMedicationDataExtractor ocrExtractor;
    private final Clock clock;

    public MedicationService(
            MedicationRepository medicationRepository,
            MedicationBlacklistRepository blacklistRepository,
            AuditService auditService,
            OcrMedicationDataExtractor ocrExtractor,
            Clock clock
    ) {
        this.medicationRepository = medicationRepository;
        this.blacklistRepository = blacklistRepository;
        this.auditService = auditService;
        this.ocrExtractor = ocrExtractor;
        this.clock = clock;
    }

    @Transactional
    public Medication create(MedicationCreateRequest request) {
        MedicationType medicationType = MedicationType.fromCode(request.medicationTypeCode());
        ensureSanitaryAcceptance(request, medicationType);

        Medication medication = new Medication(
                request.donorId(),
                request.donorCpf(),
                request.commercialName(),
                request.activeIngredient(),
                request.concentration(),
                request.manufacturer(),
                request.lotNumber(),
                request.expirationDate(),
                request.quantityAvailable(),
                medicationType,
                request.frontPhotoUrl(),
                request.blisterPhotoUrl(),
                request.storageDeclaration()
        );

        Medication saved = medicationRepository.save(medication);
        auditService.record(
                "Medication",
                saved.getId(),
                "CREATED",
                saved.getDonorCpf(),
                "Cadastro do lote " + saved.getLotNumber() + " vinculado ao CPF do doador."
        );
        return saved;
    }

    @Transactional
    public Medication updateAvailableFields(Long id, MedicationUpdateRequest request, String actorDocument) {
        Medication medication = findMedication(id);
        medication.updateAvailableData(request.quantityAvailable(), request.frontPhotoUrl(), request.blisterPhotoUrl());
        auditService.record("Medication", id, "UPDATED_AVAILABLE_FIELDS", actorDocument, "Quantidade/fotos atualizadas.");
        return medication;
    }

    @Transactional
    public void cancel(Long id, String actorDocument) {
        Medication medication = findMedication(id);
        medication.cancel();
        auditService.record("Medication", id, "SOFT_CANCELED", actorDocument, "Cancelamento logico sem exclusao fisica.");
    }

    @Transactional(readOnly = true)
    public List<Medication> search(String name, String activeIngredient, String lotNumber, LocalDate expiresBefore) {
        return medicationRepository.search(blankToNull(name), blankToNull(activeIngredient), blankToNull(lotNumber), expiresBefore);
    }

    @Transactional(readOnly = true)
    public OcrMedicationDataExtractor.OcrMedicationData previewOcr(String rawOcrText) {
        return ocrExtractor.extract(rawOcrText);
    }

    private void ensureSanitaryAcceptance(MedicationCreateRequest request, MedicationType medicationType) {
        if (!request.storageDeclaration()) {
            throw new BusinessException("Declaracao de armazenamento e obrigatoria.");
        }
        if (request.frontPhotoUrl().isBlank() || request.blisterPhotoUrl().isBlank()) {
            throw new BusinessException("Fotos da embalagem frontal e do blister/cartela sao obrigatorias.");
        }
        LocalDate minimumExpiration = LocalDate.now(clock).plusDays(MINIMUM_VALIDITY_DAYS);
        if (request.expirationDate().isBefore(minimumExpiration)) {
            throw new BusinessException("Medicamento deve ter validade minima de 45 dias.");
        }
        blacklistRepository.findBlockingItems(
                medicationType.getCode(),
                request.activeIngredient(),
                request.commercialName()
        ).stream().findFirst().ifPresent(this::rejectBlacklistedMedication);
    }

    private void rejectBlacklistedMedication(MedicationBlacklistItem item) {
        throw new BusinessException("Cadastro bloqueado: " + item.getReason());
    }

    private Medication findMedication(Long id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Medicamento nao encontrado."));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
