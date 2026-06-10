package com.wanted.backend.domain.identity.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 이메일 인증 응답")
public record EmailVerifyResponse(
        @Schema(description = "회원가입 시 사용할 이메일 인증 완료 토큰", example = "email-verification-token")
        String emailVerificationToken
) {
}