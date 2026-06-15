package com.wanted.backend.domain.identity.application.command;

public record ChangeMemberStatusCommand(
        Long memberId,
        String status,
        String memo
) {
}
