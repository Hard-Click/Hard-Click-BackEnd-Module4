package com.wanted.backend.domain.identity.application.command;

public record UpdatePasswordCommand(
        String currentPassword,
        String newPassword,
        String newPasswordConfirm
) {
}