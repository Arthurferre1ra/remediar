package br.com.remediar.web.controller;

import br.com.remediar.application.service.DonationMatchService;
import br.com.remediar.web.dto.DonationDeliveryConfirmationRequest;
import br.com.remediar.web.dto.DonationInstitutionChangeRequest;
import br.com.remediar.web.dto.DonationMatchCreateRequest;
import br.com.remediar.web.dto.DonationMatchResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/donations")
public class DonationMatchController {

    private final DonationMatchService donationMatchService;

    public DonationMatchController(DonationMatchService donationMatchService) {
        this.donationMatchService = donationMatchService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public DonationMatchResponse create(@Valid @RequestBody DonationMatchCreateRequest request) {
        return DonationMatchResponse.from(donationMatchService.create(request));
    }

    @PatchMapping("/{id}/institution")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public DonationMatchResponse changeInstitution(
            @PathVariable Long id,
            @Valid @RequestBody DonationInstitutionChangeRequest request
    ) {
        return DonationMatchResponse.from(donationMatchService.changeInstitution(
                id,
                request.institutionId(),
                request.donorLatitude(),
                request.donorLongitude(),
                request.actorDocument()
        ));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('INSTITUTION', 'ADMIN')")
    public DonationMatchResponse accept(@PathVariable Long id, Principal principal) {
        return DonationMatchResponse.from(donationMatchService.accept(id, principal.getName()));
    }

    @PostMapping("/{id}/confirm-delivery")
    @PreAuthorize("hasAnyRole('INSTITUTION', 'ADMIN')")
    public DonationMatchResponse confirmDelivery(
            @PathVariable Long id,
            @Valid @RequestBody DonationDeliveryConfirmationRequest request
    ) {
        return DonationMatchResponse.from(donationMatchService.confirmDelivery(id, request.validationCode(), request.actorDocument()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public void cancel(@PathVariable Long id, Principal principal) {
        donationMatchService.cancelOrExpire(id, principal.getName());
    }

    @GetMapping("/institution/{institutionId}")
    @PreAuthorize("hasAnyRole('INSTITUTION', 'ADMIN')")
    public List<DonationMatchResponse> searchForInstitution(
            @PathVariable Long institutionId,
            @RequestParam(required = false) String medicationName,
            @RequestParam(required = false) String lotNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDate
    ) {
        return donationMatchService.searchForInstitution(institutionId, medicationName, lotNumber, deliveryDate)
                .stream()
                .map(DonationMatchResponse::from)
                .toList();
    }

    @PostMapping("/expire-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public Integer expireOverdue() {
        return donationMatchService.expireOverdueMatches();
    }
}
