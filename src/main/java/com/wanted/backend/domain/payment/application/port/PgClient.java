package com.wanted.backend.domain.payment.application.port;

public interface PgClient {

    String confirm(Long memberId, Long courseId, Integer amount);
}
