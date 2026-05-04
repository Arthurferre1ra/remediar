package br.com.remediar.web.dto;

import jakarta.validation.constraints.NotBlank;

public record MedicationOcrPreviewRequest(@NotBlank String rawOcrText) {
}
