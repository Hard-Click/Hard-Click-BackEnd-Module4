package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.command.ResumeStudyTimerSessionCommand;
import com.wanted.backend.domain.study_timer.application.port.MemberLockPort;
import com.wanted.backend.domain.study_timer.application.usecase.ResumeStudyTimerSessionUseCase;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSessionStatus;
import com.wanted.backend.domain.study_timer.domain.repository.StudyTimerSessionRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResumeStudyTimerSessionServiceTest {

    private MemberLockPort memberLockPort;
    private StudyTimerSessionRepository repository;
    private StudyTimerSessionMetricRecorder metricRecorder;
    private ResumeStudyTimerSessionService service;

    @BeforeEach
    void setUp() {
        memberLockPort = mock(MemberLockPort.class);
        repository = mock(StudyTimerSessionRepository.class);
        metricRecorder = mock(StudyTimerSessionMetricRecorder.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T06:05:00Z"), ZoneId.of("Asia/Seoul"));
        service = new ResumeStudyTimerSessionService(memberLockPort, repository, metricRecorder, clock);
    }

    @Test
    void resumesPausedSessionAfterLockingAndLoadingOwnedSession() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        OffsetDateTime pausedAt = OffsetDateTime.parse("2026-05-11T15:03:20+09:00");
        OffsetDateTime resumedAt = OffsetDateTime.parse("2026-05-11T15:04:10+09:00");
        ResumeStudyTimerSessionCommand command = new ResumeStudyTimerSessionCommand(1L, 55L, resumedAt);
        StudyTimerSession pausedSession = new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                startedAt,
                null,
                200,
                StudyTimerSessionStatus.PAUSED,
                pausedAt
        );

        when(repository.findById(55L)).thenReturn(Optional.of(pausedSession));
        when(repository.save(any(StudyTimerSession.class)))
                .thenReturn(new StudyTimerSession(
                        55L,
                        1L,
                        null,
                        null,
                        startedAt.plusSeconds(50),
                        null,
                        200,
                        StudyTimerSessionStatus.RUNNING
                ));

        ResumeStudyTimerSessionUseCase.StudyTimerSessionResumeView result = service.handle(command);

        ArgumentCaptor<StudyTimerSession> captor = ArgumentCaptor.forClass(StudyTimerSession.class);
        InOrder inOrder = inOrder(memberLockPort, repository);
        inOrder.verify(memberLockPort).lock(1L);
        inOrder.verify(repository).findById(55L);
        inOrder.verify(repository).save(captor.capture());

        assertThat(captor.getValue().id()).isEqualTo(55L);
        assertThat(captor.getValue().status()).isEqualTo(StudyTimerSessionStatus.RUNNING);
        assertThat(captor.getValue().startedAt()).isEqualTo(startedAt.plusSeconds(50));
        assertThat(result.sessionId()).isEqualTo(55L);
        assertThat(result.status()).isEqualTo("RUNNING");
        assertThat(result.accumulatedStudySeconds()).isEqualTo(200);
        assertThat(result.resumedAt()).isEqualTo(resumedAt);
        verify(metricRecorder).recordResult(StudyTimerAction.RESUME, null);
    }

    @Test
    void businessResultIsUnaffectedWhenMetricRecordingFails() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        OffsetDateTime pausedAt = OffsetDateTime.parse("2026-05-11T15:03:20+09:00");
        OffsetDateTime resumedAt = OffsetDateTime.parse("2026-05-11T15:04:10+09:00");
        when(repository.findById(55L)).thenReturn(Optional.of(new StudyTimerSession(
                55L, 1L, null, null, startedAt, null, 200, StudyTimerSessionStatus.PAUSED, pausedAt
        )));
        when(repository.save(any(StudyTimerSession.class)))
                .thenReturn(new StudyTimerSession(
                        55L, 1L, null, null, startedAt.plusSeconds(50), null, 200, StudyTimerSessionStatus.RUNNING
                ));
        doThrow(new RuntimeException("metric registry down"))
                .when(metricRecorder).recordResult(StudyTimerAction.RESUME, null);

        ResumeStudyTimerSessionUseCase.StudyTimerSessionResumeView result =
                service.handle(new ResumeStudyTimerSessionCommand(1L, 55L, resumedAt));

        assertThat(result.sessionId()).isEqualTo(55L);
        assertThat(result.status()).isEqualTo("RUNNING");
        assertThat(result.accumulatedStudySeconds()).isEqualTo(200);
        assertThat(result.resumedAt()).isEqualTo(resumedAt);
        verify(metricRecorder).recordResult(StudyTimerAction.RESUME, null);
    }

    @Test
    void throwsNotFoundWhenSessionDoesNotExist() {
        when(repository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new ResumeStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T15:04:10+09:00")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_NOT_FOUND);

        verify(memberLockPort).lock(1L);
        verify(repository).findById(55L);
        verify(repository, never()).save(any());
        verify(metricRecorder).recordResult(StudyTimerAction.RESUME, "STUDY_TIMER_SESSION_NOT_FOUND");
    }

    @Test
    void throwsForbiddenWhenSessionOwnerDoesNotMatchLoggedInMember() {
        when(repository.findById(55L)).thenReturn(Optional.of(new StudyTimerSession(
                55L,
                2L,
                null,
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00"),
                null,
                200,
                StudyTimerSessionStatus.PAUSED,
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00")
        )));

        assertThatThrownBy(() -> service.handle(new ResumeStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T15:04:10+09:00")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(memberLockPort).lock(1L);
        verify(repository).findById(55L);
        verify(repository, never()).save(any());
        verify(metricRecorder).recordResult(StudyTimerAction.RESUME, "FORBIDDEN");
    }

    @Test
    void throwsConflictWhenSessionIsNotPaused() {
        when(repository.findById(55L)).thenReturn(Optional.of(new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00"),
                null,
                120,
                StudyTimerSessionStatus.RUNNING
        )));

        assertThatThrownBy(() -> service.handle(new ResumeStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T15:04:10+09:00")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_NOT_PAUSED);

        verify(memberLockPort).lock(1L);
        verify(repository).findById(55L);
        verify(repository, never()).save(any());
        verify(metricRecorder).recordResult(StudyTimerAction.RESUME, "STUDY_TIMER_SESSION_NOT_PAUSED");
    }

    @Test
    void throwsInvalidInputWhenResumedAtIsBeforePausedAt() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        OffsetDateTime pausedAt = OffsetDateTime.parse("2026-05-11T15:03:20+09:00");
        when(repository.findById(55L)).thenReturn(Optional.of(new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                startedAt,
                null,
                200,
                StudyTimerSessionStatus.PAUSED,
                pausedAt
        )));

        assertThatThrownBy(() -> service.handle(new ResumeStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T15:03:19+09:00")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_RESUMED_AT_BEFORE_PAUSED_AT);

        verify(memberLockPort).lock(1L);
        verify(repository).findById(55L);
        verify(repository, never()).save(any());
        verify(metricRecorder).recordResult(StudyTimerAction.RESUME, "STUDY_TIMER_RESUMED_AT_BEFORE_PAUSED_AT");
    }

    @Test
    void throwsInvalidInputWhenResumedAtIsInFuture() {
        assertThatThrownBy(() -> service.handle(new ResumeStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T15:05:01+09:00")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_RESUMED_AT_IN_FUTURE);

        verify(memberLockPort, never()).lock(any());
        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
        verify(metricRecorder).recordResult(StudyTimerAction.RESUME, "STUDY_TIMER_RESUMED_AT_IN_FUTURE");
    }

    @Test
    void throwsInvalidInputWhenResumedAtIsNull() {
        assertThatThrownBy(() -> service.handle(new ResumeStudyTimerSessionCommand(
                1L,
                55L,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_RESUMED_AT_REQUIRED);

        verify(memberLockPort, never()).lock(any());
        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
        verify(metricRecorder).recordResult(StudyTimerAction.RESUME, "STUDY_TIMER_RESUMED_AT_REQUIRED");
    }
}
