package br.com.remediar.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.remediar.domain.enums.DonationFlowStatus;
import br.com.remediar.domain.enums.MedicationStatus;
import br.com.remediar.domain.model.DonationMatch;
import br.com.remediar.domain.model.Medication;
import br.com.remediar.web.dto.DonationMatchCreateRequest;
import br.com.remediar.web.dto.MedicationCreateRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DonationMatchServiceTest {

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private DonationMatchService donationMatchService;

    @Test
    void shouldCreateAcceptAndConfirmDonationMatch() {
        Medication medication = medicationService.create(new MedicationCreateRequest(
                1L,
                "11122233344",
                "Dipirona",
                "dipirona sodica",
                "500mg",
                "EMS",
                "MATCH" + System.nanoTime(),
                LocalDate.now().plusDays(90),
                2,
                1,
                "https://files.remediar.local/front.jpg",
                "https://files.remediar.local/blister.jpg",
                true
        ));

        DonationMatch created = donationMatchService.create(new DonationMatchCreateRequest(
                medication.getId(),
                1L,
                1L,
                new BigDecimal("-23.550520"),
                new BigDecimal("-46.633308"),
                null
        ));
        assertThat(created.getStatus()).isEqualTo(DonationFlowStatus.AWAITING_ACCEPTANCE);

        DonationMatch accepted = donationMatchService.accept(created.getId(), "ong");
        assertThat(accepted.getStatus()).isEqualTo(DonationFlowStatus.AWAITING_DELIVERY);
        assertThat(accepted.getValidationCode()).startsWith("RM-" + created.getId() + "-");
        assertThat(accepted.getMedication().getStatus()).isEqualTo(MedicationStatus.DONATION_IN_PROGRESS);

        DonationMatch completed = donationMatchService.confirmDelivery(created.getId(), accepted.getValidationCode(), "12345678000199");
        assertThat(completed.getStatus()).isEqualTo(DonationFlowStatus.COMPLETED);
        assertThat(completed.getMedication().getStatus()).isEqualTo(MedicationStatus.DELIVERED);
    }
}
