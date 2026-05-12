package br.com.remediar.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.remediar.application.dto.MedicationCreateCommand;
import br.com.remediar.common.BusinessException;
import br.com.remediar.domain.enums.MedicationStatus;
import br.com.remediar.domain.model.Medication;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MedicationServiceTest {

    @Autowired
    private MedicationService medicationService;

    @Test
    void shouldCreateMedicationWhenSanitaryRulesAreValid() {
        Medication medication = medicationService.create(validRequest(1, "LOT" + System.nanoTime(), LocalDate.now().plusDays(90)));

        assertThat(medication.getId()).isNotNull();
        assertThat(medication.getStatus()).isEqualTo(MedicationStatus.AVAILABLE);
    }

    @Test
    void shouldRejectMedicationWithLessThanFortyFiveDaysOfValidity() {
        MedicationCreateCommand request = validRequest(1, "SHORT" + System.nanoTime(), LocalDate.now().plusDays(44));

        assertThatThrownBy(() -> medicationService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("validade minima");
    }

    @Test
    void shouldRejectBlacklistedMedicationType() {
        MedicationCreateCommand request = validRequest(4, "CTRL" + System.nanoTime(), LocalDate.now().plusDays(90));

        assertThatThrownBy(() -> medicationService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cadastro bloqueado");
    }

    private MedicationCreateCommand validRequest(int medicationTypeCode, String lot, LocalDate expirationDate) {
        return new MedicationCreateCommand(
                1L,
                "11122233344",
                "Dipirona",
                "dipirona sodica",
                "500mg",
                "EMS",
                lot,
                expirationDate,
                10,
                medicationTypeCode,
                "https://files.remediar.local/front.jpg",
                "https://files.remediar.local/blister.jpg",
                true
        );
    }
}
