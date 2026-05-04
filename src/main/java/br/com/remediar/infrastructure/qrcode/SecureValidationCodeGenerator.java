package br.com.remediar.infrastructure.qrcode;

import br.com.remediar.application.ports.ValidationCodeGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class SecureValidationCodeGenerator implements ValidationCodeGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate(Long donationId) {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return "RM-" + donationId + "-" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
