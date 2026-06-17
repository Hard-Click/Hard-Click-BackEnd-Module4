package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.command.SaveStudyTimerHeartbeatCommand;
import com.wanted.backend.domain.study_timer.application.port.MemberLockPort;
import com.wanted.backend.domain.study_timer.application.usecase.SaveStudyTimerHeartbeatUseCase;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSessionStatus;
import com.wanted.backend.domain.study_timer.domain.repository.StudyTimerSessionRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SaveStudyTimerHeartbeatServiceTest {

    private MemberLockPort memberLockPort;
    private StudyTimerSessionRepository repository;
    private SaveStudyTimerHeartbeatService service;

    @BeforeEach
    void setUp() {
        memberLockPort = mock(MemberLockPort.class);
        repository = mock(StudyTimerSessionRepository.class);
        service = new SaveStudyTimerHeartbeatService(memberLockPort, repository);
    }

    @Test
    void savesHeartbeatAfterLockingAndLoadingOwnedRunningSession() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        OffsetDateTime heartbeatAt = OffsetDateTime.parse("2026-05-11T15:03:20+09:00");
        SaveStudyTimerHeartbeatCommand command = new SaveStudyTimerHeartbeatCommand(1L, 55L, heartbeatAt);
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
                        StudyTimerSessionStatus.RUNNING
                ));

        SaveStudyTimerHeartbeatUseCase.StudyTimerHeartbeatView result = service.handle(command);

        ArgumentCaptor<StudyTimerSession> captor = ArgumentCaptor.forClass(StudyTimerSession.class);
        InOrder inOrder = inOrder(memberLockPort, repository);
        inOrder.verify(memberLockPort).lock(1L);
        inOrder.verify(repository).findById(55L);
        inOrder.verify(repository).save(captor.capture());

        assertThat(captor.getValue().id()).isEqualTo(55L);
        assertThat(captor.getValue().accumulatedStudySeconds()).isEqualTo(200);
        assertThat(result.sessionId()).isEqualTo(55L);
        assertThat(result.status()).isEqualTo("RUNNING");
        assertThat(result.accumulatedStudySeconds()).isEqualTo(200);
        assertThat(result.heartbeatAt()).isEqualTo(heartbeatAt);
    }

    @Test
    void throwsNotFoundWhenSessionDoesNotExist() {
        when(repository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new SaveStudyTimerHeartbeatCommand(
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

        assertThatThrownBy(() -> service.handle(new SaveStudyTimerHeartbeatCommand(
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
    void throwsConflictWhenHeartbeatTargetsEndedSession() {
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

        assertThatThrownBy(() -> service.handle(new SaveStudyTimerHeartbeatCommand(
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
    }
}
