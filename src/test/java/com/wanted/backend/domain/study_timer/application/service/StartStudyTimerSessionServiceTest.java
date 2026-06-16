package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.command.StartStudyTimerSessionCommand;
import com.wanted.backend.domain.study_timer.application.usecase.StartStudyTimerSessionUseCase;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSessionStatus;
import com.wanted.backend.domain.study_timer.domain.repository.StudyTimerSessionRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StartStudyTimerSessionServiceTest {

    private StudyTimerSessionRepository repository;
    private StartStudyTimerSessionService service;

    @BeforeEach
    void setUp() {
        repository = mock(StudyTimerSessionRepository.class);
        service = new StartStudyTimerSessionService(repository);
    }

    @Test
    void 실행_중인_세션이_없으면_순공시간_세션을_시작한다() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        StartStudyTimerSessionCommand command = new StartStudyTimerSessionCommand(1L, startedAt);

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
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().memberId()).isEqualTo(1L);
        assertThat(captor.getValue().startedAt()).isEqualTo(startedAt);
        assertThat(captor.getValue().status()).isEqualTo(StudyTimerSessionStatus.RUNNING);
        assertThat(result.sessionId()).isEqualTo(55L);
        assertThat(result.status()).isEqualTo("RUNNING");
        assertThat(result.startedAt()).isEqualTo(startedAt);
    }

    @Test
    void 이미_실행_중인_세션이_있으면_예외가_발생한다() {
        when(repository.existsRunningByMemberId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.handle(new StartStudyTimerSessionCommand(
                1L,
                OffsetDateTime.parse("2026-05-11T15:00:00+09:00")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_SESSION_ALREADY_RUNNING);
    }
}
