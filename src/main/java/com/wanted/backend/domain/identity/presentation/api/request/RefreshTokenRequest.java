package com.wanted.backend.domain.identity.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Access Token 재발급 요청")
public record RefreshTokenRequest(
        @Schema(description = "Access Token 재발급에 사용할 Refresh Token", example = "refresh-token")
        @NotBlank(message = "Refresh Token이 필요합니다")
        String refreshToken
) {
}