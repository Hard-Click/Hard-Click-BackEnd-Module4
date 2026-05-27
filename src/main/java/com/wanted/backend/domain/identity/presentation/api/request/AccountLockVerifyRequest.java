package com.wanted.backend.domain.identity.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

public record AccountLockVerifyRequest(
        @NotBlank(message = "이메일을 입력해주세요")
        String email,

        @NotBlank(message = "인증번호를 입력해주세요")
        String code
) {
}