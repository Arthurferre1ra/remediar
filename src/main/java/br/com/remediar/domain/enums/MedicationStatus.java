package br.com.remediar.domain.enums;

public enum MedicationStatus {
    AVAILABLE(1),
    DONATION_IN_PROGRESS(2),
    DELIVERED(3),
    EXPIRED_OR_CANCELED(4);

    private final int code;

    MedicationStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
