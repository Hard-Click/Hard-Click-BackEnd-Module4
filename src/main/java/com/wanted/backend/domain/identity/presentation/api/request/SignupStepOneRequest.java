package com.wanted.backend.domain.identity.presentation.api.request;

import com.wanted.backend.domain.identity.domain.policy.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupStepOneRequest(
        @NotBlank(message = "아이디를 입력해주세요")
        @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문, 숫자 조합으로 4자 이상 20자 이하여야 합니다")
        String username,

        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Pattern(regexp = PasswordPolicy.PASSWORD_REGEX, message = PasswordPolicy.PASSWORD_MESSAGE)
        String password,

        @NotBlank(message = "비밀번호 확인을 입력해주세요")
        String passwordConfirm
) {
}