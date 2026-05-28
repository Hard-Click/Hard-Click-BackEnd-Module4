package com.wanted.backend.domain.identity.presentation.api.request;

import com.wanted.backend.domain.identity.domain.policy.PasswordPolicy;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record SignupRequest(
        @NotBlank(message = "아이디를 입력해주세요")
        @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문, 숫자 조합으로 4자 이상 20자 이하여야 합니다")
        String username,

        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Pattern(regexp = PasswordPolicy.PASSWORD_REGEX, message = PasswordPolicy.PASSWORD_MESSAGE)
        String password,

        @NotBlank(message = "이름을 입력해주세요")
        String name,

        @NotBlank(message = "성별을 선택해주세요")
        String gender,

        @NotNull(message = "생년월일을 입력해주세요")
        LocalDate birthDate,

        @NotBlank(message = "전화번호를 입력해주세요")
        String phoneNumber,

        String profileImageUrl,

        @NotBlank(message = "이메일 인증이 필요합니다")
        String emailVerificationToken,
        Boolean optionalTermsAgreed
) {
}