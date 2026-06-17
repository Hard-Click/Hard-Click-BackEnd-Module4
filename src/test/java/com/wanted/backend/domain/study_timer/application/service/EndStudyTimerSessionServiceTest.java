package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.command.EndStudyTimerSessionCommand;
import com.wanted.backend.domain.study_timer.application.port.MemberLockPort;
import com.wanted.backend.domain.study_timer.application.usecase.EndStudyTimerSessionUseCase;
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

class EndStudyTimerSessionServiceTest {

    private MemberLockPort memberLockPort;
    private StudyTimerSessionRepository repository;
    private EndStudyTimerSessionService service;

    @BeforeEach
    void setUp() {
        memberLockPort = mock(MemberLockPort.class);
        repository = mock(StudyTimerSessionRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T06:10:00Z"), ZoneId.of("Asia/Seoul"));
        service = new EndStudyTimerSessionService(memberLockPort, repository, clock);
    }

    @Test
    void endsRunningSessionAfterLockingAndLoadingOwnedSession() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        OffsetDateTime endedAt = OffsetDateTime.parse("2026-05-11T15:08:20+09:00");
        EndStudyTimerSessionCommand command = new EndStudyTimerSessionCommand(1L, 55L, endedAt);
        StudyTimerSession runningSession = new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                startedAt,
                null,
                200,
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
                        endedAt,
                        500,
                        StudyTimerSessionStatus.ENDED
                ));

        EndStudyTimerSessionUseCase.StudyTimerSessionEndView result = service.handle(command);

        ArgumentCaptor<StudyTimerSession> captor = ArgumentCaptor.forClass(StudyTimerSession.class);
        InOrder inOrder = inOrder(memberLockPort, repository);
        inOrder.verify(memberLockPort).lock(1L);
        inOrder.verify(repository).findById(55L);
        inOrder.verify(repository).save(captor.capture());

        assertThat(captor.getValue().id()).isEqualTo(55L);
        assertThat(captor.getValue().status()).isEqualTo(StudyTimerSessionStatus.ENDED);
        assertThat(captor.getValue().endedAt()).isEqualTo(endedAt);
        assertThat(captor.getValue().accumulatedStudySeconds()).isEqualTo(500);
        assertThat(result.sessionId()).isEqualTo(55L);
        assertThat(result.studySeconds()).isEqualTo(500);
        assertThat(result.status()).isEqualTo("ENDED");
    }

    @Test
    void throwsNotFoundWhenSessionDoesNotExist() {
        when(repository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new EndStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T15:08:20+09:00")
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
                200,
                StudyTimerSessionStatus.RUNNING
        )));

        assertThatThrownBy(() -> service.handle(new EndStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T15:08:20+09:00")
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
                OffsetDateTime.parse("2026-05-11T15:08:20+09:00"),
                500,
                StudyTimerSessionStatus.ENDED
        )));

        assertThatThrownBy(() -> service.handle(new EndStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T15:08:20+09:00")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_NOT_RUNNING);

        verify(memberLockPort).lock(1L);
        verify(repository).findById(55L);
        verify(repository, never()).save(any());
    }

    @Test
    void throwsInvalidInputWhenEndedAtIsInFuture() {
        assertThatThrownBy(() -> service.handle(new EndStudyTimerSessionCommand(
                1L,
                55L,
                OffsetDateTime.parse("2026-05-11T15:10:01+09:00")
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("세션 종료 시각은 현재 시각 이후일 수 없습니다.");

        verify(memberLockPort, never()).lock(any());
        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }
}
