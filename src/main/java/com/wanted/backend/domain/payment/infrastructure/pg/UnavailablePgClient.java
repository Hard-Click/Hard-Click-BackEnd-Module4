package com.wanted.backend.domain.payment.infrastructure.pg;

import com.wanted.backend.domain.payment.application.port.PgClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class UnavailablePgClient implements PgClient {

    @Override
    public String confirm(Long memberId, Long courseId, Integer amount) {
        throw new IllegalStateException("Production PG client is not configured");
    }
}
