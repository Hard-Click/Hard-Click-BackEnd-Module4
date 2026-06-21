package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class EmailVerificationTokenCoordinatorTest {

    @Mock
    private EmailVerificationRepository verificationRepository;

    private EmailVerificationTokenCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator = new EmailVerificationTokenCoordinator(verificationRepository, 3);
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void retriesRedisCompletionAfterDatabaseCommit() {
        EmailVerification verification = EmailVerification.create(
                "user@example.com",
                EmailPurpose.SIGNUP,
                Duration.ofMinutes(3)
        );
        when(verificationRepository.reserveValidToken(
                eq("user@example.com"),
                eq("token"),
                eq(EmailPurpose.SIGNUP),
                anyString()
        )).thenReturn(Optional.of(verification));
        when(verificationRepository.completeTokenConsumption(
                eq("token"),
                eq(EmailPurpose.SIGNUP),
                anyString()
        )).thenReturn(false, false, true);

        coordinator.reserveForCurrentTransaction("user@example.com", "token", EmailPurpose.SIGNUP);
        TransactionSynchronizationManager.getSynchronizations().get(0).afterCommit();

        verify(verificationRepository, times(3)).completeTokenConsumption(
                eq("token"),
                eq(EmailPurpose.SIGNUP),
                anyString()
        );
    }

    @Test
    void releasesReservationWhenDatabaseTransactionRollsBack() {
        EmailVerification verification = EmailVerification.create(
                "user@example.com",
                EmailPurpose.SIGNUP,
                Duration.ofMinutes(3)
        );
        when(verificationRepository.reserveValidToken(
                eq("user@example.com"),
                eq("token"),
                eq(EmailPurpose.SIGNUP),
                anyString()
        )).thenReturn(Optional.of(verification));

        coordinator.reserveForCurrentTransaction("user@example.com", "token", EmailPurpose.SIGNUP);
        TransactionSynchronizationManager.getSynchronizations().get(0)
                .afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        verify(verificationRepository).releaseTokenReservation(
                eq("token"),
                eq(EmailPurpose.SIGNUP),
                anyString()
        );
    }

    @Test
    void completesImmediatelyWithoutTransactionSynchronization() {
        TransactionSynchronizationManager.clearSynchronization();
        EmailVerification verification = EmailVerification.create(
                "user@example.com",
                EmailPurpose.SIGNUP,
                Duration.ofMinutes(3)
        );
        when(verificationRepository.reserveValidToken(
                eq("user@example.com"),
                eq("token"),
                eq(EmailPurpose.SIGNUP),
                anyString()
        )).thenReturn(Optional.of(verification));
        when(verificationRepository.completeTokenConsumption(
                eq("token"),
                eq(EmailPurpose.SIGNUP),
                anyString()
        )).thenReturn(true);

        coordinator.reserveForCurrentTransaction("user@example.com", "token", EmailPurpose.SIGNUP);

        verify(verificationRepository).completeTokenConsumption(
                eq("token"),
                eq(EmailPurpose.SIGNUP),
                anyString()
        );
    }

    @Test
    void failsImmediatelyWhenCompletionFailsWithoutTransactionSynchronization() {
        TransactionSynchronizationManager.clearSynchronization();
        EmailVerification verification = EmailVerification.create(
                "user@example.com",
                EmailPurpose.SIGNUP,
                Duration.ofMinutes(3)
        );
        when(verificationRepository.reserveValidToken(
                eq("user@example.com"),
                eq("token"),
                eq(EmailPurpose.SIGNUP),
                anyString()
        )).thenReturn(Optional.of(verification));
        when(verificationRepository.completeTokenConsumption(
                eq("token"),
                eq(EmailPurpose.SIGNUP),
                anyString()
        )).thenReturn(false);

        assertThatThrownBy(() ->
                coordinator.reserveForCurrentTransaction("user@example.com", "token", EmailPurpose.SIGNUP)
        ).isInstanceOf(IllegalStateException.class);
    }
}
