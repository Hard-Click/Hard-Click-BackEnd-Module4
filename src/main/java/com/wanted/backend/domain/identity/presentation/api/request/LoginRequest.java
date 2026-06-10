package com.wanted.backend.domain.identity.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "로그인 아이디", example = "testuser")
        @NotBlank(message = "아이디를 입력해주세요.")
        String username,

        @Schema(description = "비밀번호", example = "Password123!")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {
}