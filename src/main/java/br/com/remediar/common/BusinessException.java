package br.com.remediar.common;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
