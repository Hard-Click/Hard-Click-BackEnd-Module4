package com.wanted.backend.domain.identity.application.command;

public record AccountLockVerifyCommand(
        String email,
        String code
) {
}