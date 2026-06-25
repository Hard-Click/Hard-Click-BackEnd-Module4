package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.command.PauseStudyTimerSessionCommand;
import com.wanted.backend.domain.study_timer.application.port.MemberLockPort;
import com.wanted.backend.domain.study_timer.application.usecase.PauseStudyTimerSessionUseCase;
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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PauseStudyTimerSessionServiceTest {

    private MemberLockPort memberLockPort;
    private StudyTimerSessionRepository repository;
    private StudyTimerSessionMetricRecorder metricRecorder;
    private PauseStudyTimerSessionService service;

    @BeforeEach
    void setUp() {
        memberLockPort = mock(MemberLockPort.class);
        repository = mock(StudyTimerSessionRepository.class);
        metricRecorder = mock(StudyTimerSessionMetricRecorder.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T06:05:00Z"), ZoneId.of("Asia/Seoul"));
        service = new PauseStudyTimerSessionService(memberLockPort, repository, metricRecorder, clock);
    }

    @Test
    void pausesRunningSessionAfterLockingAndLoadingOwnedSession() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        OffsetDateTime pausedAt = OffsetDateTime.parse("2026-05-11T15:03:20+09:00");
        PauseStudyTimerSessionCommand command = new PauseStudyTimerSessionCommand(1L, 55L, pausedAt);
        StudyTimerSession runningSession = new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                startedAt,
                null,
                120,
                StudyTimerSessionStatus.RUNNING
        );

        when(repository.findById(55L)).thenReturn(Optional.of(runningSession));
        when(repository.save(any(StudyTimerSession.class)))
                .thenReturn(new StudyTimerSession(
                        55L,
                        1L,
                        null,
                        null,
                        startedAt,
                        null,
                        200,
                        StudyTimerSessionStatus.PAUSED
                ));

        PauseStudyTimerSessionUseCase.StudyTimerSessionPauseView result = service.handle(command);

        ArgumentCaptor<StudyTimerSession> captor = ArgumentCaptor.forClass(StudyTimerSession.class);
        InOrder inOrder = inOrder(memberLockPort, repository);
        inOrder.verify(memberLockPort).lock(1L);
        inOrder.verify(repository).findById(55L);
        inOrder.verify(repository).save(captor.capture());

        assertThat(captor.getValue().id()).isEqualTo(55L);
        assertThat(captor.getValue().status()).isEqualTo(StudyTimerSessionStatus.PAUSED);
        assertThat(captor.getValue().accumulatedStudySeconds()).isEqualTo(200);
        assertThat(result.sessionId()).isEqualTo(55L);
        assertThat(result.status()).isEqualTo("PAUSED");
        assertThat(result.accumulatedStudySeconds()).isEqualTo(200);
        assertThat(result.pausedAt()).isEqualTo(pausedAt);
        verify(metricRecorder).recordSuccess(StudyTimerAction.PAUSE);
    }

    @Test
    void doesNotRecordSuccessWhenSaveFailsWithNonBusinessException() {
        // 회귀 테스트: errorCode = null이 save() 호출 전에 설정되면, save가 BusinessException이
        // 아닌 예외(예: DataAccessException)로 실패해도 catch(BusinessException)에 안 걸려서
        // 실패가 성공으로 집계되는 버그가 있었다. 이걸 잡는다.
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        OffsetDateTime pausedAt = OffsetDateTime.parse("2026-05-11T15:03:20+09:00");
        StudyTimerSession runningSession = new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                startedAt,
                null,
                120,
                StudyTimerSessionStatus.RUNNING
        );

        when(repository.findById(55L)).thenReturn(Optional.of(runningSession));
        when(repository.save(any(StudyTimerSession.class))).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.handle(new PauseStudyTimerSessionCommand(1L, 55L, pausedAt)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");

        verify(metricRecorder, never()).recordSuccess(any());
        verify(metricRecorder).recordFailure(StudyTimerAction.PAUSE, "UNKNOWN");
    }

    @Test
    void returnsExistingPausedSessionWhenPauseRequestIsRetried() {
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
                StudyTimerSessionStatus.PAUSED
        )));

        PauseStudyTimerSessionUseCase.StudyTimerSessionPauseView result = service.handle(
                new PauseStudyTimerSessionCommand(1L, 55L, pausedAt)
        );

        verify(memberLockPort).lock(1L);
        verify(repository).findById(55L);
        verify(repository, never()).save(any());
        assertThat(result.sessionId()).isEqualTo(55L);
        assertThat(result.status()).isEqualTo("PAUSED");
        assertThat(result.accumulatedStudySeconds()).isEqualTo(200);
        assertThat(result.pausedAt()).isEqualTo(pausedAt);
        verify(metricRecorder).recordSuccess(StudyTimerAction.PAUSE);
    }

    @Test
    void throwsNotFoundWhenSessionDoesNotExist() {
        when(repository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new PauseStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_NOT_FOUND);

        verify(memberLockPort).lock(1L);
        verify(repository).findById(55L);
        verify(repository, never()).save(any());
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
                120,
                StudyTimerSessionStatus.RUNNING
        )));

        assertThatThrownBy(() -> service.handle(new PauseStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(memberLockPort).lock(1L);
        verify(repository).findById(55L);
        verify(repository, never()).save(any());
    }

    @Test
    void throwsConflictWhenSessionIsNotRunning() {
        when(repository.findById(55L)).thenReturn(Optional.of(new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00"),
                OffsetDateTime.parse("2026-05-11T15:10:00+09:00"),
                600,
                StudyTimerSessionStatus.ENDED
        )));

        assertThatThrownBy(() -> service.handle(new PauseStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T15:03:20+09:00")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_NOT_RUNNING);

        verify(memberLockPort).lock(1L);
        verify(repository).findById(55L);
        verify(repository, never()).save(any());
        verify(metricRecorder).recordFailure(StudyTimerAction.PAUSE, "STUDY_TIMER_SESSION_NOT_RUNNING");
    }

    @Test
    void throwsInvalidInputWhenPausedAtIsBeforeStartedAt() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        when(repository.findById(55L)).thenReturn(Optional.of(new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                startedAt,
                null,
                120,
                StudyTimerSessionStatus.RUNNING
        )));

        assertThatThrownBy(() -> service.handle(new PauseStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T14:59:59+09:00")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_PAUSED_AT_BEFORE_STARTED_AT);

        verify(memberLockPort).lock(1L);
        verify(repository).findById(55L);
        verify(repository, never()).save(any());
    }

    @Test
    void throwsInvalidInputWhenPausedAtIsInFuture() {
        assertThatThrownBy(() -> service.handle(new PauseStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T15:05:01+09:00")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_PAUSED_AT_IN_FUTURE);

        verify(memberLockPort, never()).lock(any());
        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void throwsInvalidInputWhenPausedAtIsNull() {
        assertThatThrownBy(() -> service.handle(new PauseStudyTimerSessionCommand(
                1L,
                55L,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_PAUSED_AT_REQUIRED);

        verify(memberLockPort, never()).lock(any());
        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }
}
