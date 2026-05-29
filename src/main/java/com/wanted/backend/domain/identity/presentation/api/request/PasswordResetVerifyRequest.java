package com.wanted.backend.domain.identity.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 재설정 인증번호 검증 요청")
public record PasswordResetVerifyRequest(
        @Schema(description = "비밀번호 재설정 인증번호를 받은 이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력해주세요")
        String email,

        @Schema(description = "이메일로 받은 6자리 인증번호", example = "123456")
        @NotBlank(message = "인증번호를 입력해주세요")
        String code
) {
}