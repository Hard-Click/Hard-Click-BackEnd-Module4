package com.wanted.backend.domain.study_timer.domain.model;

import java.time.OffsetDateTime;

public class StudyTimerSession {

    private final Long id;
    private final Long memberId;
    private final Long courseId;
    private final Long lessonId;
    private final OffsetDateTime startedAt;
    private final OffsetDateTime endedAt;
    private final Integer elapsedSeconds;
    private final StudyTimerSessionStatus status;

    public StudyTimerSession(
            Long id,
            Long memberId,
            Long courseId,
            Long lessonId,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt,
            Integer elapsedSeconds,
            StudyTimerSessionStatus status
    ) {
        validate(memberId, startedAt, elapsedSeconds, status);
        this.id = id;
        this.memberId = memberId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.elapsedSeconds = elapsedSeconds;
        this.status = status;
    }

    public static StudyTimerSession start(Long memberId, OffsetDateTime startedAt) {
        return new StudyTimerSession(
                null,
                memberId,
                null,
                null,
                startedAt,
                null,
                0,
                StudyTimerSessionStatus.RUNNING
        );
    }

    private static void validate(
            Long memberId,
            OffsetDateTime startedAt,
            Integer elapsedSeconds,
            StudyTimerSessionStatus status
    ) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        if (startedAt == null) {
            throw new IllegalArgumentException("세션 시작 시각은 필수입니다.");
        }
        if (elapsedSeconds == null) {
            throw new IllegalArgumentException("경과 시간은 필수입니다.");
        }
        if (status == null) {
            throw new IllegalArgumentException("세션 상태는 필수입니다.");
        }

        if (elapsedSeconds < 0) {
            throw new IllegalArgumentException("경과 시간은 0 이상이어야 합니다.");
        }
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

    public Long lessonId() {
        return lessonId;
    }

    public OffsetDateTime startedAt() {
        return startedAt;
    }

    public OffsetDateTime endedAt() {
        return endedAt;
    }

    public Integer elapsedSeconds() {
        return elapsedSeconds;
    }

    public StudyTimerSessionStatus status() {
        return status;
    }
}
