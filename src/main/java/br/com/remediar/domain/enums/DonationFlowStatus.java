package br.com.remediar.domain.enums;

public enum DonationFlowStatus {
    AWAITING_ACCEPTANCE(1),
    AWAITING_DELIVERY(2),
    COMPLETED(3),
    EXPIRED(4),
    CANCELED(5);

    private final int code;

    DonationFlowStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
