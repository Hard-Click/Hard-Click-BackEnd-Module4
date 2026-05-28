package com.wanted.backend.domain.identity.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "Refresh Token이 필요합니다")
        String refreshToken
) {
}