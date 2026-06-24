package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.command.StartStudyTimerSessionCommand;
import com.wanted.backend.domain.study_timer.application.port.MemberLockPort;
import com.wanted.backend.domain.study_timer.application.usecase.StartStudyTimerSessionUseCase;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StartStudyTimerSessionServiceTest {

    private MemberLockPort memberLockPort;
    private StudyTimerSessionRepository repository;
    private StartStudyTimerSessionService service;

    @BeforeEach
    void setUp() {
        memberLockPort = mock(MemberLockPort.class);
        repository = mock(StudyTimerSessionRepository.class);
        service = new StartStudyTimerSessionService(memberLockPort, repository);
    }

    @Test
    void startsSessionAfterAcquiringLockAndCheckingExistingActiveSession() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        StartStudyTimerSessionCommand command = new StartStudyTimerSessionCommand(1L, startedAt);

        when(repository.existsActiveByMemberId(1L)).thenReturn(false);
        when(repository.save(any(StudyTimerSession.class)))
                .thenReturn(new StudyTimerSession(
                        55L,
                        1L,
                        null,
                        null,
                        startedAt,
                        null,
                        0,
                        StudyTimerSessionStatus.RUNNING
                ));

        StartStudyTimerSessionUseCase.StudyTimerSessionStartView result = service.handle(command);

        ArgumentCaptor<StudyTimerSession> captor = ArgumentCaptor.forClass(StudyTimerSession.class);
        InOrder inOrder = inOrder(memberLockPort, repository);
        inOrder.verify(memberLockPort).lock(1L);
        inOrder.verify(repository).existsActiveByMemberId(1L);
        inOrder.verify(repository).save(captor.capture());

        assertThat(captor.getValue().memberId()).isEqualTo(1L);
        assertThat(captor.getValue().startedAt()).isEqualTo(startedAt);
        assertThat(captor.getValue().status()).isEqualTo(StudyTimerSessionStatus.RUNNING);
        assertThat(result.sessionId()).isEqualTo(55L);
        assertThat(result.status()).isEqualTo("RUNNING");
        assertThat(result.startedAt()).isEqualTo(startedAt);
    }

    @Test
    void throwsConflictWhenActiveSessionAlreadyExistsAfterLocking() {
        when(repository.existsActiveByMemberId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.handle(new StartStudyTimerSessionCommand(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_ALREADY_RUNNING);

        verify(memberLockPort).lock(1L);
        verify(repository).existsActiveByMemberId(1L);
        verify(repository, never()).save(any());
    }
}
