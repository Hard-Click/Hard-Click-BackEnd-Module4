package com.wanted.backend.domain.learning_activity.application.command;

public record VideoPlayCommand(
        Long memberId,
        Long videoId
) {
}
