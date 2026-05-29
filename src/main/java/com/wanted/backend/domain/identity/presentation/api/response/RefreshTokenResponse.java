package com.wanted.backend.domain.identity.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Access Token 재발급 응답")
public record RefreshTokenResponse(
        @Schema(description = "새로 발급된 Access Token", example = "access-token")
        String accessToken
) {
}