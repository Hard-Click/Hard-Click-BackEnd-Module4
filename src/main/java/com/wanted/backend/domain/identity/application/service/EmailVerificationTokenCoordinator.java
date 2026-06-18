package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Component
@Slf4j
public class EmailVerificationTokenCoordinator {

    private final EmailVerificationRepository verificationRepository;
    private final int completionRetryAttempts;

    public EmailVerificationTokenCoordinator(
            EmailVerificationRepository verificationRepository,
            @Value("${identity.email.completion-retry-attempts}") int completionRetryAttempts
    ) {
        this.verificationRepository = verificationRepository;
        this.completionRetryAttempts = Math.max(1, completionRetryAttempts);
    }

    public void reserveForCurrentTransaction(String email, String token, EmailPurpose purpose) {
        String reservationId = UUID.randomUUID().toString();
        verificationRepository.reserveValidToken(email, token, purpose, reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            complete(token, purpose, reservationId);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                completeAfterCommit(token, purpose, reservationId);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    verificationRepository.releaseTokenReservation(token, purpose, reservationId);
                }
            }
        });
    }

    private void complete(String token, EmailPurpose purpose, String reservationId) {
        if (!verificationRepository.completeTokenConsumption(token, purpose, reservationId)) {
            throw new IllegalStateException("Failed to consume reserved email verification token");
        }
    }

    private void completeAfterCommit(String token, EmailPurpose purpose, String reservationId) {
        for (int attempt = 1; attempt <= completionRetryAttempts; attempt++) {
            try {
                if (verificationRepository.completeTokenConsumption(token, purpose, reservationId)) {
                    if (attempt > 1) {
                        log.info("Email verification token consumption recovered on retry. purpose={}, attempt={}",
                                purpose, attempt);
                    }
                    return;
                }
                log.warn("Email verification token consumption returned false after DB commit. purpose={}, attempt={}/{}",
                        purpose, attempt, completionRetryAttempts);
            } catch (RuntimeException exception) {
                log.warn("Email verification token consumption failed after DB commit. purpose={}, attempt={}/{}",
                        purpose, attempt, completionRetryAttempts, exception);
            }
            if (attempt < completionRetryAttempts) {
                try {
                    Thread.sleep(50L * attempt);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.error("Email verification token remains reserved after all completion retries. purpose={}, reservationId={}",
                purpose, reservationId);
    }
}
