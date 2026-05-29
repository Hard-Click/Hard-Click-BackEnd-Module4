package com.wanted.backend.domain.identity.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class EmailVerification {
    private final Long id;
    private final String email;
    private final String code;
    private final EmailPurpose purpose;
    private boolean isVerified;
    private String verificationToken;
    private boolean used;
    private final LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;

    private EmailVerification(Long id, String email, String code, EmailPurpose purpose,
                              boolean isVerified, String verificationToken, boolean used,
                              LocalDateTime expiresAt, LocalDateTime verifiedAt) {
        this.id = id;
        this.email = email;
        this.code = code;
        this.purpose = purpose;
        this.isVerified = isVerified;
        this.verificationToken = verificationToken;
        this.used = used;
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
                false,
                null,
                false,
                LocalDateTime.now().plusMinutes(3),
                null
        );
    }

    public static EmailVerification restore(Long id, String email, String code, EmailPurpose purpose,
                                            boolean isVerified, String verificationToken, boolean used,
                                            LocalDateTime expiresAt, LocalDateTime verifiedAt) {
        return new EmailVerification(
                id,
                email,
                code,
                purpose,
                isVerified,
                verificationToken,
                used,
                expiresAt,
                verifiedAt
        );
    }

    public void verify(String inputCode) {
        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw new RuntimeException("인증 번호 유효시간(3분)이 만료되었습니다.");
        }
        if (!this.code.equals(inputCode)) {
            throw new RuntimeException("인증번호가 올바르지 않습니다.");
        }
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
        this.verificationToken = UUID.randomUUID().toString();
        this.used = false;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(this.expiresAt);
    }

    public void useToken() {
        this.used = true;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getCode() { return code; }
    public EmailPurpose getPurpose() { return purpose; }
    public boolean isVerified() { return isVerified; }
    public String getVerificationToken() { return verificationToken; }
    public boolean isUsed() { return used; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
}