package com.wanted.backend.domain.identity.domain.model;

import java.time.LocalDateTime;

public class RefreshToken {

    private final Long id;
    private final Long memberId;
    private final String token;
    private final LocalDateTime expiryDate;
    private final LocalDateTime createdAt;

    public RefreshToken(
            Long id,
            Long memberId,
            String token,
            LocalDateTime expiryDate,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.token = token;
        this.expiryDate = expiryDate;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}