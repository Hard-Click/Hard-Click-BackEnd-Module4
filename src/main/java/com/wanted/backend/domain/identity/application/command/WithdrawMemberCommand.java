package com.wanted.backend.domain.identity.application.command;

public record WithdrawMemberCommand(
        String currentPassword
) {
}