package com.wanted.backend.domain.learning_activity.domain.model;

import java.time.LocalDateTime;

public class VideoProgress {

    private final Long id;
    private final Long memberId;
    private final Long courseId;
    private final Long videoId;
    private final Integer lastPositionSec;
    private final Integer watchTimeSec;
    private final Boolean completed;
    private final LocalDateTime completedAt;

    public VideoProgress(
            Long id,
            Long memberId,
            Long courseId,
            Long videoId,
            Integer lastPositionSec,
            Integer watchTimeSec,
            Boolean completed,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.courseId = courseId;
        this.videoId = videoId;
        this.lastPositionSec = lastPositionSec;
        this.watchTimeSec = watchTimeSec;
        this.completed = completed;
        this.completedAt = completedAt;
    }

    public static VideoProgress empty(Long memberId, Long courseId, Long videoId) {
        return new VideoProgress(null, memberId, courseId, videoId, 0, 0, false, null);
    }

    public VideoProgress updateLastPosition(Integer lastPositionSec) {
        return new VideoProgress(
                id,
                memberId,
                courseId,
                videoId,
                lastPositionSec,
                watchTimeSec,
                completed,
                completedAt
        );
    }

    public Long id() {
        return id;
    }

    public Long memberId() {
        return memberId;
    }

    public Long courseId() {
        return courseId;
    }

    public Long videoId() {
        return videoId;
    }

    public Integer lastPositionSec() {
        return lastPositionSec;
    }

    public Integer watchTimeSec() {
        return watchTimeSec;
    }

    public Boolean completed() {
        return completed;
    }

    public LocalDateTime completedAt() {
        return completedAt;
    }

    public boolean isCompleted() {
        return Boolean.TRUE.equals(completed);
    }
}
