package br.com.remediar.infrastructure.ocr;

import br.com.remediar.application.ports.OcrMedicationDataExtractor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class RegexOcrMedicationDataExtractor implements OcrMedicationDataExtractor {

    private static final Pattern LOT_PATTERN = Pattern.compile("(?i)(lote|lot)\\s*[:#-]?\\s*([A-Z0-9.-]{3,40})");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{2}/\\d{2}/\\d{4}|\\d{4}-\\d{2}-\\d{2})");
    private static final DateTimeFormatter BRAZILIAN_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public OcrMedicationData extract(String ocrText) {
        String source = ocrText == null ? "" : ocrText;
        return new OcrMedicationData(extractLot(source), extractExpirationDate(source));
    }

    private Optional<String> extractLot(String source) {
        Matcher matcher = LOT_PATTERN.matcher(source);
        return matcher.find() ? Optional.of(matcher.group(2).trim()) : Optional.empty();
    }

    private Optional<LocalDate> extractExpirationDate(String source) {
        Matcher matcher = DATE_PATTERN.matcher(source);
        while (matcher.find()) {
            String value = matcher.group(1);
            try {
                return Optional.of(value.contains("/")
                        ? LocalDate.parse(value, BRAZILIAN_DATE)
                        : LocalDate.parse(value));
            } catch (DateTimeParseException ignored) {
                // Continue scanning OCR text because noisy captures can contain malformed dates.
            }
        }
        return Optional.empty();
    }
}
