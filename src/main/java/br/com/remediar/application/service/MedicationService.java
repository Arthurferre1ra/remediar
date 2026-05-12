package br.com.remediar.application.service;

import br.com.remediar.application.dto.MedicationCreateCommand;
import br.com.remediar.application.dto.MedicationUpdateCommand;
import br.com.remediar.application.ports.OcrMedicationDataExtractor;
import br.com.remediar.common.BusinessException;
import br.com.remediar.common.NotFoundException;
import br.com.remediar.domain.enums.MedicationType;
import br.com.remediar.domain.model.Medication;
import br.com.remediar.domain.model.MedicationBlacklistItem;
import br.com.remediar.domain.repository.MedicationBlacklistRepository;
import br.com.remediar.domain.repository.MedicationRepository;
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
    public Medication create(MedicationCreateCommand command) {
        MedicationType medicationType = MedicationType.fromCode(command.medicationTypeCode());
        ensureSanitaryAcceptance(command, medicationType);

        Medication medication = new Medication(
                command.donorId(),
                command.donorCpf(),
                command.commercialName(),
                command.activeIngredient(),
                command.concentration(),
                command.manufacturer(),
                command.lotNumber(),
                command.expirationDate(),
                command.quantityAvailable(),
                medicationType,
                command.frontPhotoUrl(),
                command.blisterPhotoUrl(),
                command.storageDeclaration()
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
    public Medication updateAvailableFields(Long id, MedicationUpdateCommand command, String actorDocument) {
        Medication medication = findMedication(id);
        medication.updateAvailableData(command.quantityAvailable(), command.frontPhotoUrl(), command.blisterPhotoUrl());
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

    private void ensureSanitaryAcceptance(MedicationCreateCommand command, MedicationType medicationType) {
        if (!command.storageDeclaration()) {
            throw new BusinessException("Declaracao de armazenamento e obrigatoria.");
        }
        if (command.frontPhotoUrl().isBlank() || command.blisterPhotoUrl().isBlank()) {
            throw new BusinessException("Fotos da embalagem frontal e do blister/cartela sao obrigatorias.");
        }
        LocalDate minimumExpiration = LocalDate.now(clock).plusDays(MINIMUM_VALIDITY_DAYS);
        if (command.expirationDate().isBefore(minimumExpiration)) {
            throw new BusinessException("Medicamento deve ter validade minima de 45 dias.");
        }
        blacklistRepository.findBlockingItems(
                medicationType.getCode(),
                command.activeIngredient(),
                command.commercialName()
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
