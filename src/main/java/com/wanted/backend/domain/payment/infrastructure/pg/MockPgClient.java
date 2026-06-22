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
@Profile({"local", "test"})
public class MockPgClient implements PgClient {

    private static final double DEFAULT_TIMEOUT_PROBABILITY = 0.05;
    private static final double TIMEOUT_PROBABILITY = resolveTimeoutProbability();

    private static double resolveTimeoutProbability() {
        String raw = System.getProperty("pg.mock.timeout-probability");
        if (raw == null) {
            return DEFAULT_TIMEOUT_PROBABILITY;
        }
        try {
            double parsed = Double.parseDouble(raw);
            return Math.max(0.0, Math.min(1.0, parsed));
        } catch (NumberFormatException e) {
            return DEFAULT_TIMEOUT_PROBABILITY;
        }
    }

    @Override
    public String confirm(Long memberId, Long courseId, Integer amount) {
        if (ThreadLocalRandom.current().nextDouble() < TIMEOUT_PROBABILITY) {
            throw new PgTimeoutException("Mock PG 타임아웃 (memberId=" + memberId + ", courseId=" + courseId + ")");
        }
        return UUID.randomUUID().toString();
    }

    public static class PgTimeoutException extends RuntimeException {
        public PgTimeoutException(String message) {
            super(message);
        }
    }
}
