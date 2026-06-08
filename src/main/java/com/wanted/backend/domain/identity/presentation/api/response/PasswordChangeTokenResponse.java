package com.wanted.backend.domain.identity.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 변경 토큰 응답")
public record PasswordChangeTokenResponse(
        @Schema(description = "비밀번호 변경에 사용할 인증 완료 토큰", example = "password-change-token")
        String passwordChangeToken
) {
}