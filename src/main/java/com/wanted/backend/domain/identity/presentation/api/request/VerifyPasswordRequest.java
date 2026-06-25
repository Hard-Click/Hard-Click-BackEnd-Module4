package com.wanted.backend.domain.identity.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "현재 비밀번호 검증 요청")
public record VerifyPasswordRequest(
        @Schema(description = "현재 비밀번호", example = "Password123!")
        @NotBlank(message = "현재 비밀번호를 입력해주세요")
        String currentPassword
) {
}
