package com.wanted.backend.domain.study_timer.domain.model;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudyTimerSessionTest {

    @Test
    void startRejectsNullMemberId() {
        assertThatThrownBy(() -> StudyTimerSession.start(
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 필수입니다.");
    }

    @Test
    void startRejectsNullStartedAt() {
        assertThatThrownBy(() -> StudyTimerSession.start(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("세션 시작 시각은 필수입니다.");
    }

    @Test
    void constructorRejectsNegativeElapsedSeconds() {
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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("경과 시간은 0 이상이어야 합니다.");
    }

    @Test
    void heartbeatUpdatesElapsedSecondsFromStartedAt() {
        StudyTimerSession session = StudyTimerSession.start(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        );

        StudyTimerSession updated = session.heartbeat(
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00")
        );

        assertThat(updated.accumulatedStudySeconds()).isEqualTo(200);
        assertThat(updated.status()).isEqualTo(StudyTimerSessionStatus.RUNNING);
    }

    @Test
    void heartbeatDoesNotDecreaseElapsedSecondsWhenOlderRequestArrives() {
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
                OffsetDateTime.parse("2026-05-11T15:02:30+09:00")
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
                OffsetDateTime.parse("2026-05-11T14:59:59+09:00")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("하트비트 시각은 세션 시작 시각 이후여야 합니다.");
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
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_NOT_RUNNING);
    }
}
