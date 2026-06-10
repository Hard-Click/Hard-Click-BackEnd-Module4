package com.wanted.backend.domain.identity.presentation.api.request;

import com.wanted.backend.domain.identity.domain.policy.PasswordPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "비밀번호 재설정 요청")
public record ResetPasswordRequest(
        @Schema(description = "비밀번호를 재설정할 이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력해주세요")
        String email,

        @Schema(description = "비밀번호 재설정 인증 후 발급받은 비밀번호 변경 토큰", example = "password-change-token")
        @NotBlank(message = "인증 토큰이 필요합니다")
        String passwordChangeToken,

        @Schema(description = "새 비밀번호", example = "NewPassword123!")
        @NotBlank(message = "새 비밀번호를 입력해주세요")
        @Pattern(regexp = PasswordPolicy.PASSWORD_REGEX, message = PasswordPolicy.PASSWORD_MESSAGE)
        String newPassword,

        @Schema(description = "새 비밀번호 확인", example = "NewPassword123!")
        @NotBlank(message = "비밀번호 확인을 입력해주세요")
        String newPasswordConfirm
) {
}