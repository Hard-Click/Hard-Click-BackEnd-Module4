package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EmailVerificationCleanupScheduler {

    private final EmailVerificationRepository verificationRepository;

    @Value("${identity.email.cleanup-retention-days:7}")
    private long cleanupRetentionDays;

    @Scheduled(cron = "${identity.email.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void cleanupExpiredVerifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(cleanupRetentionDays);
        verificationRepository.deleteByExpiresAtBefore(cutoff);
    }
}
