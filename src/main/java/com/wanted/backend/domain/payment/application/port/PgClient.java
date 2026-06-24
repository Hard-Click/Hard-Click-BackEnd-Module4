package com.wanted.backend.domain.payment.application.port;

public interface PgClient {

    String confirm(String paymentKey, String orderId, Integer amount);
}
