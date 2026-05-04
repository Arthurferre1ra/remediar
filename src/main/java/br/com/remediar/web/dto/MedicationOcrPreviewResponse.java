package br.com.remediar.web.dto;

import br.com.remediar.application.ports.OcrMedicationDataExtractor;
import java.time.LocalDate;

public record MedicationOcrPreviewResponse(String lotNumber, LocalDate expirationDate) {

    public static MedicationOcrPreviewResponse from(OcrMedicationDataExtractor.OcrMedicationData data) {
        return new MedicationOcrPreviewResponse(
                data.lotNumber().orElse(null),
                data.expirationDate().orElse(null)
        );
    }
}
