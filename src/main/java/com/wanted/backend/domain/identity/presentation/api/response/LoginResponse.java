package com.wanted.backend.domain.identity.presentation.api.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long memberId,
        String role
) {
}