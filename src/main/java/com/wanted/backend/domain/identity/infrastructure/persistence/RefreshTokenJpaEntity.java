package com.wanted.backend.domain.identity.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected RefreshTokenJpaEntity() {
    }

    public RefreshTokenJpaEntity(String token, Long memberId, LocalDateTime expiryDate, LocalDateTime createdAt) {
        this.token = token;
        this.memberId = memberId;
        this.expiryDate = expiryDate;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }


    public Long getId() { return id; }
    public String getToken() { return token; }
    public Long getMemberId() { return memberId; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}