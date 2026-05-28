package com.wanted.backend.domain.identity.presentation.api.request;

import com.wanted.backend.domain.identity.application.command.UpdatePasswordCommand;
import com.wanted.backend.domain.identity.domain.policy.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePasswordRequest(
        @NotBlank(message = "현재 비밀번호를 입력해주세요")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요")
        @Pattern(regexp = PasswordPolicy.PASSWORD_REGEX, message = PasswordPolicy.PASSWORD_MESSAGE)
        String newPassword,

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