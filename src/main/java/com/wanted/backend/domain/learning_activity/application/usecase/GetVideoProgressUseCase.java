package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface GetVideoProgressUseCase {

    VideoProgressView handle(MemberVideoCommand command);

    record VideoProgressView(
            Long videoId,
            Integer lastPositionSeconds,
            Integer watchTimeSeconds,
            Integer durationSeconds,
            BigDecimal progressRate,
            Boolean completed,
            LocalDateTime completedAt
    ) {
    }
}
