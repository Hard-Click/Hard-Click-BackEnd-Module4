package com.wanted.backend.domain.identity.domain.repository;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;

import java.time.LocalDateTime;
import java.util.Optional;

public interface pository {
    void save(EmailVerification verification);

    Optional<EmailVerification> findLatestPendingByEmailAndPurpose(String email, EmailPurpose purpose);

    Optional<EmailVerification> findByVerificationTokenAndPurpose(String token, EmailPurpose purpose);

    Optional<EmailVerification> reserveValidToken(
            String email,
            String token,
            EmailPurpose purpose,
            String reservationId
    );

    boolean completeTokenConsumption(String token, EmailPurpose purpose, String reservationId);

    void releaseTokenReservation(String token, EmailPurpose purpose, String reservationId);

    boolean tryAcquireSendPermission(
            String email,
            EmailPurpose purpose,
            int dailyLimit,
            LocalDateTime expiresAt
    );

    void revokeActiveByEmailAndPurpose(String email, EmailPurpose purpose);
}
