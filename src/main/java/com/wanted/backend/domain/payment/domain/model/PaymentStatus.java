package com.wanted.backend.domain.payment.domain.model;

public enum PaymentStatus {
    PENDING,
    PAID,
    REFUNDED,
    READY,
    FAILED,
    CANCELED,
    CANCELLED;

    public static PaymentStatus from(String value) {
        return PaymentStatus.valueOf(value);
    }
}
