package br.com.remediar.domain.enums;

import br.com.remediar.common.BusinessException;

public enum MedicationType {
    OVER_THE_COUNTER(1),
    PRESCRIPTION(2),
    ANTIBIOTIC(3),
    CONTROLLED_SPECIAL(4);

    private final int code;

    MedicationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MedicationType fromCode(int code) {
        for (MedicationType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new BusinessException("Tipo de medicamento invalido.");
    }
}
