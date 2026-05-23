package com.wanted.backend.domain.learning_activity.application.command;

public record GetVideoProgressCommand(
        Long memberId,
        Long videoId
) {
}
