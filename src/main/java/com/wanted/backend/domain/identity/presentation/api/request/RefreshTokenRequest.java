package com.wanted.backend.domain.identity.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh Token이 필요합니다")
        String refreshToken
) {
}