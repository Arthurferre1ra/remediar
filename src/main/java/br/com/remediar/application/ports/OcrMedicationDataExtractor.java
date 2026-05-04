package br.com.remediar.application.ports;

import java.time.LocalDate;
import java.util.Optional;

public interface OcrMedicationDataExtractor {

    OcrMedicationData extract(String ocrText);

    record OcrMedicationData(Optional<String> lotNumber, Optional<LocalDate> expirationDate) {
    }
}
