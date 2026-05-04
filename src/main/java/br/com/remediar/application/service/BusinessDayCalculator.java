package br.com.remediar.application.service;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class BusinessDayCalculator {

    public LocalDate addBusinessDays(LocalDate start, int days) {
        LocalDate date = start;
        int added = 0;
        while (added < days) {
            date = date.plusDays(1);
            switch (date.getDayOfWeek()) {
                case SATURDAY, SUNDAY -> {
                }
                default -> added++;
            }
        }
        return date;
    }
}
