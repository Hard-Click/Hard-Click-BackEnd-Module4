package com.wanted.backend.domain.payment.infrastructure.pg;

import com.wanted.backend.domain.payment.application.port.PgClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 실제 PG 연동 없이 결제 승인을 흉내내는 Mock 클라이언트.
 * 5% 확률로 타임아웃을 시뮬레이션해 PG 장애 상황을 재현한다.
 * local/test 프로파일에서만 활성화된다.
 */
@Component
@Profile({"local", "test", "default"})
public class MockPgClient implements PgClient {

    private static final double TIMEOUT_PROBABILITY = 0.05;

    @Override
    public String confirm(String paymentKey, String orderId, Integer amount) {
        if (ThreadLocalRandom.current().nextDouble() < TIMEOUT_PROBABILITY) {
            throw new PgTimeoutException("Mock PG 타임아웃 (orderId=" + orderId + ")");
        }
        return paymentKey != null ? paymentKey : UUID.randomUUID().toString();
    }

    public static class PgTimeoutException extends RuntimeException {
        public PgTimeoutException(String message) {
            super(message);
        }
    }
}
