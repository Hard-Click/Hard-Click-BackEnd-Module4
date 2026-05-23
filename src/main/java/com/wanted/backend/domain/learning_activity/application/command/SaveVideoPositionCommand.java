package com.wanted.backend.domain.learning_activity.application.command;

public record SaveVideoPositionCommand(
        Long videoId,
        Integer positionSeconds
) {
}
