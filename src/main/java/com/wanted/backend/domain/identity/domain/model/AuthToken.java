package com.wanted.backend.domain.identity.domain.model;

public record AuthToken(
        String accessToken,
        String refreshToken,
        Long memberId,
        String role
) {
}