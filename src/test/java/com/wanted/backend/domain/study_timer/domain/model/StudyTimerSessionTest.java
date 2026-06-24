package com.wanted.backend.domain.study_timer.domain.model;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudyTimerSessionTest {

    private static final OffsetDateTime SERVER_NOW = OffsetDateTime.parse("2026-05-11T15:05:00+09:00");

    @Test
    void startRejectsNullMemberId() {
        assertThatThrownBy(() -> StudyTimerSession.start(
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_MEMBER_ID_REQUIRED);
    }

    @Test
    void startRejectsNullStartedAt() {
        assertThatThrownBy(() -> StudyTimerSession.start(1L, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_STARTED_AT_REQUIRED);
    }

    @Test
    void constructorRejectsNegativeAccumulatedStudySeconds() {
        assertThatThrownBy(() -> new StudyTimerSession(
                1L,
                1L,
                null,
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00"),
                null,
                -1,
                StudyTimerSessionStatus.RUNNING
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_INVALID);
    }

    @Test
    void heartbeatUpdatesAccumulatedStudySecondsFromStartedAt() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );

        StudyTimerSession updated = session.heartbeat(
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00"),
                SERVER_NOW
        );

        assertThat(updated.accumulatedStudySeconds()).isEqualTo(200);
        assertThat(updated.status()).isEqualTo(StudyTimerSessionStatus.RUNNING);
    }

    @Test
    void heartbeatDoesNotDecreaseAccumulatedStudySecondsWhenOlderRequestArrives() {
        StudyTimerSession session = new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00"),
                null,
                200,
                StudyTimerSessionStatus.RUNNING
        );

        StudyTimerSession updated = session.heartbeat(
                OffsetDateTime.parse("2026-05-11T15:02:30+09:00"),
                SERVER_NOW
        );

        assertThat(updated.accumulatedStudySeconds()).isEqualTo(200);
    }

    @Test
    void heartbeatRejectsEarlierThanStartedAt() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );

        assertThatThrownBy(() -> session.heartbeat(
                OffsetDateTime.parse("2026-05-11T14:59:59+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_HEARTBEAT_AT_BEFORE_STARTED_AT);
    }

    @Test
    void heartbeatRejectsFutureHeartbeatAt() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );

        assertThatThrownBy(() -> session.heartbeat(
                OffsetDateTime.parse("2026-05-11T15:05:01+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_HEARTBEAT_AT_IN_FUTURE);
    }

    @Test
    void heartbeatRejectsEndedSession() {
        StudyTimerSession session = new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00"),
                OffsetDateTime.parse("2026-05-11T15:10:00+09:00"),
                600,
                StudyTimerSessionStatus.ENDED
        );

        assertThatThrownBy(() -> session.heartbeat(
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_NOT_RUNNING);
    }

    @Test
    void heartbeatRejectsPausedSession() {
        StudyTimerSession session = new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00"),
                null,
                200,
                StudyTimerSessionStatus.PAUSED
        );

        assertThatThrownBy(() -> session.heartbeat(
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_NOT_RUNNING);
    }

    @Test
    void pauseUpdatesStatusPausedAndAccumulatedStudySeconds() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );

        StudyTimerSession paused = session.pause(
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00"),
                SERVER_NOW
        );

        assertThat(paused.status()).isEqualTo(StudyTimerSessionStatus.PAUSED);
        assertThat(paused.accumulatedStudySeconds()).isEqualTo(200);
        assertThat(paused.endedAt()).isNull();
    }

    @Test
    void pauseDoesNotDecreaseAccumulatedStudySecondsWhenOlderRequestArrives() {
        StudyTimerSession session = new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00"),
                null,
                200,
                StudyTimerSessionStatus.RUNNING
        );

        StudyTimerSession paused = session.pause(
                OffsetDateTime.parse("2026-05-11T15:02:30+09:00"),
                SERVER_NOW
        );

        assertThat(paused.accumulatedStudySeconds()).isEqualTo(200);
        assertThat(paused.status()).isEqualTo(StudyTimerSessionStatus.PAUSED);
    }

    @Test
    void pauseRejectsEarlierThanStartedAt() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );

        assertThatThrownBy(() -> session.pause(
                OffsetDateTime.parse("2026-05-11T14:59:59+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_PAUSED_AT_BEFORE_STARTED_AT);
    }

    @Test
    void pauseRejectsFuturePausedAt() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );

        assertThatThrownBy(() -> session.pause(
                OffsetDateTime.parse("2026-05-11T15:05:01+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_PAUSED_AT_IN_FUTURE);
    }

    @Test
    void pauseRejectsNullPausedAt() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );

        assertThatThrownBy(() -> session.pause(null, SERVER_NOW))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_PAUSED_AT_REQUIRED);
    }

    @Test
    void pauseRejectsEndedSession() {
        StudyTimerSession session = new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00"),
                OffsetDateTime.parse("2026-05-11T15:10:00+09:00"),
                600,
                StudyTimerSessionStatus.ENDED
        );

        assertThatThrownBy(() -> session.pause(
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_NOT_RUNNING);
    }

    @Test
    void pauseRejectsPausedSession() {
        StudyTimerSession session = new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00"),
                null,
                200,
                StudyTimerSessionStatus.PAUSED
        );

        assertThatThrownBy(() -> session.pause(
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_NOT_RUNNING);
    }

    @Test
    void resumeTransitionsPausedSessionBackToRunningAndShiftsStartedAt() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        StudyTimerSession session = StudyTimerSession.start(1L, startedAt);
        StudyTimerSession paused = session.pause(
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00"),
                SERVER_NOW
        );

        StudyTimerSession resumed = paused.resume(
                OffsetDateTime.parse("2026-05-11T15:04:10+09:00"),
                SERVER_NOW
        );

        assertThat(resumed.status()).isEqualTo(StudyTimerSessionStatus.RUNNING);
        assertThat(resumed.accumulatedStudySeconds()).isEqualTo(200);
        assertThat(resumed.pausedAt()).isNull();
        assertThat(resumed.startedAt()).isEqualTo(startedAt.plusSeconds(50));
    }

    @Test
    void resumeExcludesPausedDurationFromSubsequentHeartbeat() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );
        StudyTimerSession paused = session.pause(
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00"),
                SERVER_NOW
        );
        StudyTimerSession resumed = paused.resume(
                OffsetDateTime.parse("2026-05-11T15:04:10+09:00"),
                SERVER_NOW
        );

        StudyTimerSession afterHeartbeat = resumed.heartbeat(
                OffsetDateTime.parse("2026-05-11T15:04:40+09:00"),
                SERVER_NOW
        );

        assertThat(afterHeartbeat.accumulatedStudySeconds()).isEqualTo(230);
    }

    @Test
    void resumeRejectsRunningSession() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );

        assertThatThrownBy(() -> session.resume(
                OffsetDateTime.parse("2026-05-11T15:04:00+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_NOT_PAUSED);
    }

    @Test
    void resumeRejectsEndedSession() {
        StudyTimerSession session = new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00"),
                OffsetDateTime.parse("2026-05-11T15:10:00+09:00"),
                600,
                StudyTimerSessionStatus.ENDED
        );

        assertThatThrownBy(() -> session.resume(
                OffsetDateTime.parse("2026-05-11T15:11:00+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_NOT_PAUSED);
    }

    @Test
    void resumeRejectsNullResumedAt() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        ).pause(OffsetDateTime.parse("2026-05-11T15:03:20+09:00"), SERVER_NOW);

        assertThatThrownBy(() -> session.resume(null, SERVER_NOW))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_RESUMED_AT_REQUIRED);
    }

    @Test
    void resumeRejectsFutureResumedAt() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        ).pause(OffsetDateTime.parse("2026-05-11T15:03:20+09:00"), SERVER_NOW);

        assertThatThrownBy(() -> session.resume(
                OffsetDateTime.parse("2026-05-11T15:05:01+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_RESUMED_AT_IN_FUTURE);
    }

    @Test
    void resumeRejectsResumedAtBeforePausedAt() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        ).pause(OffsetDateTime.parse("2026-05-11T15:03:20+09:00"), SERVER_NOW);

        assertThatThrownBy(() -> session.resume(
                OffsetDateTime.parse("2026-05-11T15:03:19+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_RESUMED_AT_BEFORE_PAUSED_AT);
    }

    @Test
    void endUpdatesStatusEndedAndAccumulatedStudySeconds() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );

        OffsetDateTime endedAt = OffsetDateTime.parse("2026-05-11T15:03:20+09:00");
        StudyTimerSession ended = session.end(endedAt, SERVER_NOW);

        assertThat(ended.status()).isEqualTo(StudyTimerSessionStatus.ENDED);
        assertThat(ended.endedAt()).isEqualTo(endedAt);
        assertThat(ended.accumulatedStudySeconds()).isEqualTo(200);
    }

    @Test
    void endDoesNotDecreaseAccumulatedStudySecondsWhenOlderRequestArrives() {
        StudyTimerSession session = new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00"),
                null,
                200,
                StudyTimerSessionStatus.RUNNING
        );

        StudyTimerSession ended = session.end(
                OffsetDateTime.parse("2026-05-11T15:02:30+09:00"),
                SERVER_NOW
        );

        assertThat(ended.accumulatedStudySeconds()).isEqualTo(200);
        assertThat(ended.status()).isEqualTo(StudyTimerSessionStatus.ENDED);
    }

    @Test
    void endRejectsEarlierThanStartedAt() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );

        assertThatThrownBy(() -> session.end(
                OffsetDateTime.parse("2026-05-11T14:59:59+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_ENDED_AT_BEFORE_STARTED_AT);
    }

    @Test
    void endRejectsFutureEndedAt() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );

        assertThatThrownBy(() -> session.end(
                OffsetDateTime.parse("2026-05-11T15:05:01+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_ENDED_AT_IN_FUTURE);
    }

    @Test
    void endRejectsNullEndedAt() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );

        assertThatThrownBy(() -> session.end(null, SERVER_NOW))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_ENDED_AT_REQUIRED);
    }

    @Test
    void endRejectsEndedSession() {
        StudyTimerSession session = new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00"),
                OffsetDateTime.parse("2026-05-11T15:10:00+09:00"),
                600,
                StudyTimerSessionStatus.ENDED
        );

        assertThatThrownBy(() -> session.end(
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00"),
                SERVER_NOW
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_NOT_RUNNING);
    }
}
