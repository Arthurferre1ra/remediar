package br.com.remediar.web.controller;

import br.com.remediar.application.service.MedicationService;
import br.com.remediar.infrastructure.security.UserPrincipal;
import br.com.remediar.web.dto.MedicationCreateRequest;
import br.com.remediar.web.dto.MedicationOcrPreviewRequest;
import br.com.remediar.web.dto.MedicationOcrPreviewResponse;
import br.com.remediar.web.dto.MedicationResponse;
import br.com.remediar.web.dto.MedicationUpdateRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/medications")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping("/ocr-preview")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public MedicationOcrPreviewResponse previewOcr(@Valid @RequestBody MedicationOcrPreviewRequest request) {
        return MedicationOcrPreviewResponse.from(medicationService.previewOcr(request.rawOcrText()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public MedicationResponse create(@Valid @RequestBody MedicationCreateRequest request) {
        return MedicationResponse.from(medicationService.create(request.toCommand()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public MedicationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody MedicationUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return MedicationResponse.from(medicationService.updateAvailableFields(id, request.toCommand(), principal.actorDocument()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public void cancel(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        medicationService.cancel(id, principal.actorDocument());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MedicationResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String activeIngredient,
            @RequestParam(required = false) String lotNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresBefore
    ) {
        return medicationService.search(name, activeIngredient, lotNumber, expiresBefore)
                .stream()
                .map(MedicationResponse::from)
                .toList();
    }
}
