package com.wanted.backend.domain.identity.domain.model;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

public class EmailVerification {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final Long id;
    private final String email;
    private final String codeHash;
    private final String rawCode;
    private final EmailPurpose purpose;
    private VerificationStatus status;
    private String verificationToken;
    private final LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;

    private EmailVerification(
            Long id,
            String email,
            String codeHash,
            String rawCode,
            EmailPurpose purpose,
            VerificationStatus status,
            String verificationToken,
            LocalDateTime expiresAt,
            LocalDateTime verifiedAt
    ) {
        this.id = id;
        this.email = email;
        this.codeHash = codeHash;
        this.rawCode = rawCode;
        this.purpose = purpose;
        this.status = status;
        this.verificationToken = verificationToken;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
    }

    public static EmailVerification create(String email, EmailPurpose purpose, Duration codeTtl) {
        if (codeTtl == null || codeTtl.isZero() || codeTtl.isNegative()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        String code = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        return new EmailVerification(
                null,
                email,
                hash(code),
                code,
                purpose,
                VerificationStatus.PENDING,
                null,
                LocalDateTime.now().plus(codeTtl),
                null
        );
    }

    public static EmailVerification restore(
            Long id,
            String email,
            String codeHash,
            EmailPurpose purpose,
            VerificationStatus status,
            String verificationToken,
            LocalDateTime expiresAt,
            LocalDateTime verifiedAt
    ) {
        return new EmailVerification(
                id,
                email,
                codeHash,
                null,
                purpose,
                status,
                verificationToken,
                expiresAt,
                verifiedAt
        );
    }

    public void verify(String inputCode) {
        LocalDateTime now = LocalDateTime.now();

        if (this.status != VerificationStatus.PENDING) {
            throw new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND);
        }
        if (now.isAfter(expiresAt)) {
            expire();
            throw new BusinessException(ErrorCode.VERIFICATION_EXPIRED);
        }
        if (!MessageDigest.isEqual(
                codeHash.getBytes(StandardCharsets.UTF_8),
                hash(inputCode).getBytes(StandardCharsets.UTF_8)
        )) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_MISMATCH);
        }

        this.status = VerificationStatus.VERIFIED;
        this.verifiedAt = now;
        this.verificationToken = UUID.randomUUID().toString();
    }

    public void useToken() {
        if (this.status != VerificationStatus.VERIFIED) {
            throw new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND);
        }
        if (LocalDateTime.now().isAfter(expiresAt)) {
            expire();
            throw new BusinessException(ErrorCode.VERIFICATION_EXPIRED);
        }
        this.status = VerificationStatus.USED;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(this.expiresAt);
    }

    public void expire() {
        if (this.status == VerificationStatus.PENDING || this.status == VerificationStatus.VERIFIED) {
            this.status = VerificationStatus.EXPIRED;
        }
    }

    public void revoke() {
        if (this.status == VerificationStatus.PENDING || this.status == VerificationStatus.VERIFIED) {
            this.status = VerificationStatus.REVOKED;
        }
    }

    public boolean isVerified() {
        return this.status == VerificationStatus.VERIFIED;
    }

    public boolean isUsed() {
        return this.status == VerificationStatus.USED;
    }

    public boolean isPending() {
        return this.status == VerificationStatus.PENDING;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getCode() {
        if (rawCode == null) {
            throw new IllegalStateException("Raw verification code is only available immediately after creation");
        }
        return rawCode;
    }
    public String getCodeHash() { return codeHash; }
    public EmailPurpose getPurpose() { return purpose; }
    public VerificationStatus getStatus() { return status; }
    public String getVerificationToken() { return verificationToken; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }

    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }
}
