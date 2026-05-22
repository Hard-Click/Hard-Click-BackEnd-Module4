package com.wanted.backend.domain.learning_activity.domain.model;

import java.time.LocalDateTime;

public record VideoProgress(
        Long id,
        Long memberId,
        Long courseId,
        Long videoId,
        Integer lastPositionSec,
        Integer watchTimeSec,
        Boolean completed,
        LocalDateTime completedAt
) {

    public static VideoProgress empty(Long memberId, Long courseId, Long videoId) {
        return new VideoProgress(null, memberId, courseId, videoId, 0, 0, false, null);
    }

    public boolean isCompleted() {
        return Boolean.TRUE.equals(completed);
    }
}
