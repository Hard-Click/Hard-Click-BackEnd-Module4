package com.wanted.backend.domain.identity.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class EmailVerification {
    private final Long id;
    private final String email;
    private final String code;
    private final EmailPurpose purpose;
    private VerificationStatus status;
    private String verificationToken;
    private final LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;

    private EmailVerification(Long id, String email, String code, EmailPurpose purpose,
                              VerificationStatus status, String verificationToken,
                              LocalDateTime expiresAt, LocalDateTime verifiedAt) {
        this.id = id;
        this.email = email;
        this.code = code;
        this.purpose = purpose;
        this.status = status;
        this.verificationToken = verificationToken;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
    }

    public static EmailVerification create(String email, EmailPurpose purpose) {
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        return new EmailVerification(
                null,
                email,
                code,
                purpose,
                VerificationStatus.PENDING,
                null,
                LocalDateTime.now().plusMinutes(3),
                null
        );
    }

    public static EmailVerification restore(Long id, String email, String code, EmailPurpose purpose,
                                            VerificationStatus status, String verificationToken,
                                            LocalDateTime expiresAt, LocalDateTime verifiedAt) {
        return new EmailVerification(
                id,
                email,
                code,
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
            throw new RuntimeException("검증할 수 없는 인증 요청입니다.");
        }

        if (now.isAfter(expiresAt)) {
            expire();
            throw new RuntimeException("인증 번호 유효시간(3분)이 만료되었습니다.");
        }

        if (!this.code.equals(inputCode)) {
            throw new RuntimeException("인증번호가 올바르지 않습니다.");
        }

        this.status = VerificationStatus.VERIFIED;
        this.verifiedAt = now;
        this.verificationToken = UUID.randomUUID().toString();
    }

    public void useToken() {
        if (this.status != VerificationStatus.VERIFIED) {
            throw new RuntimeException("사용할 수 없는 인증 토큰입니다.");
        }

        if (LocalDateTime.now().isAfter(expiresAt)) {
            expire();
            throw new RuntimeException("인증 토큰이 만료되었습니다.");
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
    public String getCode() { return code; }
    public EmailPurpose getPurpose() { return purpose; }
    public VerificationStatus getStatus() { return status; }
    public String getVerificationToken() { return verificationToken; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
}