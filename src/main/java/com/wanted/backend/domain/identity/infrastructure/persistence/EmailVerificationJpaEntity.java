package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.VerificationStatus;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications", indexes = {
        @Index(name = "idx_email_purpose_created", columnList = "email, purpose, created_at"),
        @Index(name = "idx_token", columnList = "verification_token")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmailPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(nullable = false)
    private boolean isVerified = false;

    @Column(length = 200)
    private String verificationToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime verifiedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void updateFromDomain(com.wanted.backend.domain.identity.domain.model.EmailVerification domain) {
        this.status = domain.getStatus();
        this.isVerified = domain.isVerified();
        this.verificationToken = domain.getVerificationToken();
        this.used = domain.isUsed();
        this.verifiedAt = domain.getVerifiedAt();
    }

    @Builder
    public EmailVerificationJpaEntity(String email, String code, EmailPurpose purpose, LocalDateTime expiresAt) {
        this.email = email;
        this.code = code;
        this.purpose = purpose;
        this.status = VerificationStatus.PENDING;
        this.expiresAt = expiresAt;
    }
}