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
    private final OffsetDateTime pausedAt;

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
        this(id, memberId, courseId, lessonId, startedAt, endedAt, accumulatedStudySeconds, status, null);
    }

    public StudyTimerSession(
            Long id,
            Long memberId,
            Long courseId,
            Long lessonId,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt,
            Integer accumulatedStudySeconds,
            StudyTimerSessionStatus status,
            OffsetDateTime pausedAt
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
        this.pausedAt = pausedAt;
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
        long calculatedAccumulatedStudySeconds = calculateAccumulatedStudySeconds(
                heartbeatAt,
                serverNow,
                ErrorCode.STUDY_TIMER_HEARTBEAT_AT_REQUIRED,
                ErrorCode.STUDY_TIMER_HEARTBEAT_AT_IN_FUTURE,
                ErrorCode.STUDY_TIMER_HEARTBEAT_AT_BEFORE_STARTED_AT
        );

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

    public StudyTimerSession pause(OffsetDateTime pausedAt, OffsetDateTime serverNow) {
        long calculatedAccumulatedStudySeconds = calculateAccumulatedStudySeconds(
                pausedAt,
                serverNow,
                ErrorCode.STUDY_TIMER_PAUSED_AT_REQUIRED,
                ErrorCode.STUDY_TIMER_PAUSED_AT_IN_FUTURE,
                ErrorCode.STUDY_TIMER_PAUSED_AT_BEFORE_STARTED_AT
        );

        return new StudyTimerSession(
                id,
                memberId,
                courseId,
                lessonId,
                startedAt,
                endedAt,
                Math.max(accumulatedStudySeconds, (int) calculatedAccumulatedStudySeconds),
                StudyTimerSessionStatus.PAUSED,
                pausedAt
        );
    }

    public StudyTimerSession resume(OffsetDateTime resumedAt, OffsetDateTime serverNow) {
        if (status != StudyTimerSessionStatus.PAUSED) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_SESSION_NOT_PAUSED);
        }
        if (resumedAt == null) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_RESUMED_AT_REQUIRED);
        }
        if (serverNow == null) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_SESSION_INVALID);
        }
        if (resumedAt.toInstant().isAfter(serverNow.toInstant())) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_RESUMED_AT_IN_FUTURE);
        }
        if (resumedAt.toInstant().isBefore(pausedAt.toInstant())) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_RESUMED_AT_BEFORE_PAUSED_AT);
        }

        // 일시정지 구간(pausedAt~resumedAt)을 startedAt에 더해서 밀어내면,
        // 이후 heartbeat/end의 Duration.between(startedAt, 측정시각) 계산에서
        // 정지 시간이 자동으로 제외된다.
        Duration pausedDuration = Duration.between(pausedAt.toInstant(), resumedAt.toInstant());
        OffsetDateTime adjustedStartedAt = startedAt.plus(pausedDuration);

        return new StudyTimerSession(
                id,
                memberId,
                courseId,
                lessonId,
                adjustedStartedAt,
                endedAt,
                accumulatedStudySeconds,
                StudyTimerSessionStatus.RUNNING,
                null
        );
    }

    public StudyTimerSession end(OffsetDateTime endedAt, OffsetDateTime serverNow) {
        long calculatedAccumulatedStudySeconds = calculateAccumulatedStudySeconds(
                endedAt,
                serverNow,
                ErrorCode.STUDY_TIMER_ENDED_AT_REQUIRED,
                ErrorCode.STUDY_TIMER_ENDED_AT_IN_FUTURE,
                ErrorCode.STUDY_TIMER_ENDED_AT_BEFORE_STARTED_AT
        );

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

    private long calculateAccumulatedStudySeconds(
            OffsetDateTime measuredAt,
            OffsetDateTime serverNow,
            ErrorCode measuredAtRequiredErrorCode,
            ErrorCode measuredAtInFutureErrorCode,
            ErrorCode measuredAtBeforeStartedAtErrorCode
    ) {
        validateMeasuredAt(measuredAt, serverNow, measuredAtRequiredErrorCode, measuredAtInFutureErrorCode);
        validateRunning();

        long calculatedAccumulatedStudySeconds = Duration.between(startedAt.toInstant(), measuredAt.toInstant()).getSeconds();
        if (calculatedAccumulatedStudySeconds < 0) {
            throw new BusinessException(measuredAtBeforeStartedAtErrorCode);
        }
        if (calculatedAccumulatedStudySeconds > Integer.MAX_VALUE) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_SESSION_INVALID);
        }

        return calculatedAccumulatedStudySeconds;
    }

    private void validateMeasuredAt(
            OffsetDateTime measuredAt,
            OffsetDateTime serverNow,
            ErrorCode measuredAtRequiredErrorCode,
            ErrorCode measuredAtInFutureErrorCode
    ) {
        if (measuredAt == null) {
            throw new BusinessException(measuredAtRequiredErrorCode);
        }
        if (serverNow == null) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_SESSION_INVALID);
        }
        if (measuredAt.toInstant().isAfter(serverNow.toInstant())) {
            throw new BusinessException(measuredAtInFutureErrorCode);
        }
    }

    private void validateRunning() {
        if (status != StudyTimerSessionStatus.RUNNING) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_SESSION_NOT_RUNNING);
        }
    }

    private static void validate(
            Long memberId,
            OffsetDateTime startedAt,
            Integer accumulatedStudySeconds,
            StudyTimerSessionStatus status
    ) {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_MEMBER_ID_REQUIRED);
        }
        if (startedAt == null) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_STARTED_AT_REQUIRED);
        }
        if (accumulatedStudySeconds == null || accumulatedStudySeconds < 0 || status == null) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_SESSION_INVALID);
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

    public OffsetDateTime pausedAt() {
        return pausedAt;
    }
}
