package com.wanted.backend.domain.identity.presentation.api.request;

import com.wanted.backend.domain.identity.application.command.UpdatePasswordCommand;
import com.wanted.backend.domain.identity.domain.policy.PasswordPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "로그인 사용자 비밀번호 변경 요청")
public record UpdatePasswordRequest(
        @Schema(description = "현재 비밀번호", example = "Password123!")
        @NotBlank(message = "현재 비밀번호를 입력해주세요")
        String currentPassword,

        @Schema(description = "새 비밀번호", example = "NewPassword123!")
        @NotBlank(message = "새 비밀번호를 입력해주세요")
        @Pattern(regexp = PasswordPolicy.PASSWORD_REGEX, message = PasswordPolicy.PASSWORD_MESSAGE)
        String newPassword,

        @Schema(description = "새 비밀번호 확인", example = "NewPassword123!")
        @NotBlank(message = "비밀번호 확인을 입력해주세요")
        String newPasswordConfirm
) {
    public UpdatePasswordCommand toCommand() {
        return new UpdatePasswordCommand(
                currentPassword,
                newPassword,
                newPasswordConfirm
        );
    }
}