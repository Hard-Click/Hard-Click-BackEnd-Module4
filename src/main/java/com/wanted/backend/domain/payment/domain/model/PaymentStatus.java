package com.wanted.backend.domain.payment.domain.model;

public enum PaymentStatus {
    PAID,
    REFUNDED,
    READY,
    FAILED,
    CANCELED;

    public static PaymentStatus from(String value) {
        return PaymentStatus.valueOf(value);
    }
}
