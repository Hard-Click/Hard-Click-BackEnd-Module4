package com.wanted.backend.domain.identity.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원가입 이메일 인증번호 검증 요청")
public record EmailVerifyRequest(
        @Schema(description = "회원가입 인증을 진행할 이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @Schema(description = "이메일로 받은 6자리 인증번호", example = "123456")
        @NotBlank(message = "인증번호를 입력해주세요")
        String code
) {
}