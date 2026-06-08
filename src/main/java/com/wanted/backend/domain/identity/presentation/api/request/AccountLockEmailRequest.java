package com.wanted.backend.domain.identity.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "계정 잠금 인증번호 발송 요청")
public record AccountLockEmailRequest(
        @Schema(description = "계정 잠금 인증번호를 받을 이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        String email
) {
}