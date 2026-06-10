package com.wanted.backend.domain.identity.presentation.api.request;

import com.wanted.backend.domain.identity.domain.policy.PasswordPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Schema(description = "회원가입 요청")
public record SignupRequest(
        @Schema(description = "회원가입 아이디", example = "testuser")
        @NotBlank(message = "아이디를 입력해주세요")
        @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문, 숫자 조합으로 4자 이상 20자 이하여야 합니다")
        String username,

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @Schema(description = "비밀번호", example = "Password123!")
        @NotBlank(message = "비밀번호를 입력해주세요")
        @Pattern(regexp = PasswordPolicy.PASSWORD_REGEX, message = PasswordPolicy.PASSWORD_MESSAGE)
        String password,

        @Schema(description = "이름", example = "홍길동")
        @NotBlank(message = "이름을 입력해주세요")
        String name,

        @Schema(description = "성별", example = "MALE")
        @NotBlank(message = "성별을 선택해주세요")
        String gender,

        @Schema(description = "생년월일", example = "1999-01-01")
        @NotNull(message = "생년월일을 입력해주세요")
        LocalDate birthDate,

        @Schema(description = "전화번호", example = "010-1234-5678")
        @NotBlank(message = "전화번호를 입력해주세요")
        String phoneNumber,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.png")
        String profileImageUrl,

        @Schema(description = "회원가입 이메일 인증 완료 토큰", example = "email-verification-token")
        @NotBlank(message = "이메일 인증이 필요합니다")
        String emailVerificationToken,

        @Schema(description = "선택 약관 동의 여부", example = "true")
        Boolean optionalTermsAgreed
) {
}