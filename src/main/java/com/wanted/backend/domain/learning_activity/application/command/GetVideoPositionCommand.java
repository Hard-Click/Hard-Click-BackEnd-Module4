package com.wanted.backend.domain.learning_activity.application.command;

public record GetVideoPositionCommand(
        Long memberId,
        Long videoId
) {
}
