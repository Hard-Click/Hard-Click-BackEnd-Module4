package com.wanted.backend.domain.order.domain.model;

public enum OrderStatus {
    READY,
    PAID,
    PARTIAL_REFUNDED,
    REFUNDED,
    CANCELED,
    FAILED
}
