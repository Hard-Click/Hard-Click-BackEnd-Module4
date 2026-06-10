package com.wanted.backend.domain.learning_activity.application.command;

public record MemberVideoCommand(
        Long memberId,
        Long videoId
) {
}
