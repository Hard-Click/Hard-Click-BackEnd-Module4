package com.wanted.backend.domain.learning_activity.application.command;

public record SaveWatchTimeCommand(
        Long memberId,
        Long videoId,
        Integer watchTimeSeconds
) {
}
