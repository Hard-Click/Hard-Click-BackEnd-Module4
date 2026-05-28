package com.wanted.backend.domain.learning_activity.application.command;

public record CompleteVideoCommand(
        Long memberId,
        Long videoId
) {
}
