package com.wanted.backend.domain.identity.application.command;

public record AccountLockPasswordChangeCommand(
        String passwordChangeToken,
        String newPassword,
        String newPasswordConfirm
) {
}