package com.wanted.backend.domain.identity.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답")
public record SignupResponse(
        @Schema(description = "가입된 회원 ID", example = "1")
        Long memberId
) {
}