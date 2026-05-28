package com.wanted.backend.domain.identity.application.command;

public record ResetPasswordCommand(
        String email,
        String passwordChangeToken,
        String newPassword,
        String newPasswordConfirm
) {
}