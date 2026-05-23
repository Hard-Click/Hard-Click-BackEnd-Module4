package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.GetVideoProgressCommand;

import java.time.LocalDateTime;

public interface GetVideoProgressUseCase {

    VideoProgressView handle(GetVideoProgressCommand command);

    record VideoProgressView(
            Long videoId,
            Integer lastPositionSeconds,
            Integer watchTimeSeconds,
            Boolean completed,
            LocalDateTime completedAt
    ) {
    }
}
