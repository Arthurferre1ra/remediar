package br.com.remediar.infrastructure.scheduler;

import br.com.remediar.application.service.DonationMatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DonationExpiryScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationExpiryScheduler.class);

    private final DonationMatchService donationMatchService;

    public DonationExpiryScheduler(DonationMatchService donationMatchService) {
        this.donationMatchService = donationMatchService;
    }

    @Scheduled(cron = "0 15 2 * * *")
    public void expireOverdueMatches() {
        int expired = donationMatchService.expireOverdueMatches();
        if (expired > 0) {
            LOGGER.info("Expired {} overdue donation matches.", expired);
        }
    }
}
