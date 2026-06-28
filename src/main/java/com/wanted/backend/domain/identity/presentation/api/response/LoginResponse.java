package com.wanted.backend.domain.identity.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(
        @Schema(description = "Access Token", example = "access-token")
        String accessToken,

        @Schema(description = "Refresh Token", example = "refresh-token")
        String refreshToken,

        @Schema(description = "회원 ID", example = "1")
        Long memberId,

        @Schema(description = "회원 권한 (STUDENT, INSTRUCTOR, ADMIN)", example = "STUDENT")
        String role
) {
}