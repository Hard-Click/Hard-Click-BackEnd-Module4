package com.wanted.backend.domain.study_timer.domain.model;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;

import java.time.Duration;
import java.time.OffsetDateTime;

public class StudyTimerSession {

    private final Long id;
    private final Long memberId;
    private final Long courseId;
    private final Long lessonId;
    private final OffsetDateTime startedAt;
    private final OffsetDateTime endedAt;
    private final Integer accumulatedStudySeconds;
    private final StudyTimerSessionStatus status;

    public StudyTimerSession(
            Long id,
            Long memberId,
            Long courseId,
            Long lessonId,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt,
            Integer accumulatedStudySeconds,
            StudyTimerSessionStatus status
    ) {
        validate(memberId, startedAt, accumulatedStudySeconds, status);
        this.id = id;
        this.memberId = memberId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.accumulatedStudySeconds = accumulatedStudySeconds;
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

    public StudyTimerSession heartbeat(OffsetDateTime heartbeatAt, OffsetDateTime serverNow) {
        if (heartbeatAt == null) {
            throw new IllegalArgumentException("하트비트 시각은 필수입니다.");
        }
        if (serverNow == null) {
            throw new IllegalArgumentException("서버 현재 시각은 필수입니다.");
        }
        if (heartbeatAt.toInstant().isAfter(serverNow.toInstant())) {
            throw new IllegalArgumentException("하트비트 시각은 현재 시각 이후일 수 없습니다.");
        }
        if (status != StudyTimerSessionStatus.RUNNING) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_SESSION_NOT_RUNNING);
        }

        long calculatedAccumulatedStudySeconds = Duration.between(startedAt.toInstant(), heartbeatAt.toInstant()).getSeconds();
        if (calculatedAccumulatedStudySeconds < 0) {
            throw new IllegalArgumentException("하트비트 시각은 세션 시작 시각 이후여야 합니다.");
        }
        if (calculatedAccumulatedStudySeconds > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("경과 시간이 허용 범위를 초과했습니다.");
        }

        return new StudyTimerSession(
                id,
                memberId,
                courseId,
                lessonId,
                startedAt,
                endedAt,
                Math.max(accumulatedStudySeconds, (int) calculatedAccumulatedStudySeconds),
                status
        );
    }

    public StudyTimerSession end(OffsetDateTime endedAt, OffsetDateTime serverNow) {
        if (endedAt == null) {
            throw new IllegalArgumentException("세션 종료 시각은 필수입니다.");
        }
        if (serverNow == null) {
            throw new IllegalArgumentException("서버 현재 시각은 필수입니다.");
        }
        if (endedAt.toInstant().isAfter(serverNow.toInstant())) {
            throw new IllegalArgumentException("세션 종료 시각은 현재 시각 이후일 수 없습니다.");
        }
        if (status != StudyTimerSessionStatus.RUNNING) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_SESSION_NOT_RUNNING);
        }

        long calculatedAccumulatedStudySeconds = Duration.between(startedAt.toInstant(), endedAt.toInstant()).getSeconds();
        if (calculatedAccumulatedStudySeconds < 0) {
            throw new IllegalArgumentException("세션 종료 시각은 세션 시작 시각 이후여야 합니다.");
        }
        if (calculatedAccumulatedStudySeconds > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("누적 순공시간이 허용 범위를 초과했습니다.");
        }

        return new StudyTimerSession(
                id,
                memberId,
                courseId,
                lessonId,
                startedAt,
                endedAt,
                Math.max(accumulatedStudySeconds, (int) calculatedAccumulatedStudySeconds),
                StudyTimerSessionStatus.ENDED
        );
    }

    private static void validate(
            Long memberId,
            OffsetDateTime startedAt,
            Integer accumulatedStudySeconds,
            StudyTimerSessionStatus status
    ) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        if (startedAt == null) {
            throw new IllegalArgumentException("세션 시작 시각은 필수입니다.");
        }
        if (accumulatedStudySeconds == null) {
            throw new IllegalArgumentException("경과 시간은 필수입니다.");
        }
        if (status == null) {
            throw new IllegalArgumentException("세션 상태는 필수입니다.");
        }

        if (accumulatedStudySeconds < 0) {
            throw new IllegalArgumentException("경과 시간은 0 이상이어야 합니다.");
        }
    }

    public Long id() {
        return id;
    }

    public Long memberId() {
        return memberId;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.memberId.equals(memberId);
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

    public Integer accumulatedStudySeconds() {
        return accumulatedStudySeconds;
    }

    public StudyTimerSessionStatus status() {
        return status;
    }
}
