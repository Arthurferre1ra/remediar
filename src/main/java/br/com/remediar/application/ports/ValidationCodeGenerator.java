package br.com.remediar.application.ports;

public interface ValidationCodeGenerator {

    String generate(Long donationId);
}
