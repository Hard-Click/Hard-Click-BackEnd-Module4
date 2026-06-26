package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.query.GetCurrentStudyTimerSessionQuery;
import com.wanted.backend.domain.study_timer.application.usecase.GetCurrentStudyTimerSessionUseCase;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSessionStatus;
import com.wanted.backend.domain.study_timer.domain.repository.StudyTimerSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetCurrentStudyTimerSessionServiceTest {

    private StudyTimerSessionRepository repository;
    private GetCurrentStudyTimerSessionService service;

    @BeforeEach
    void setUp() {
        repository = mock(StudyTimerSessionRepository.class);
        service = new GetCurrentStudyTimerSessionService(repository);
    }

    @Test
    void returnsCurrentRunningSession() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        when(repository.findActiveByMemberId(1L)).thenReturn(Optional.of(new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                startedAt,
                null,
                200,
                StudyTimerSessionStatus.RUNNING
        )));

        GetCurrentStudyTimerSessionUseCase.CurrentStudyTimerSessionView result =
                service.handle(new GetCurrentStudyTimerSessionQuery(1L));

        assertThat(result.sessionId()).isEqualTo(55L);
        assertThat(result.status()).isEqualTo("RUNNING");
        assertThat(result.startedAt()).isEqualTo(startedAt);
        assertThat(result.accumulatedStudySeconds()).isEqualTo(200);
        verify(repository).findActiveByMemberId(1L);
    }

    @Test
    void returnsCurrentPausedSession() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        when(repository.findActiveByMemberId(1L)).thenReturn(Optional.of(new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                startedAt,
                null,
                200,
                StudyTimerSessionStatus.PAUSED
        )));

        GetCurrentStudyTimerSessionUseCase.CurrentStudyTimerSessionView result =
                service.handle(new GetCurrentStudyTimerSessionQuery(1L));

        assertThat(result.sessionId()).isEqualTo(55L);
        assertThat(result.status()).isEqualTo("PAUSED");
        assertThat(result.startedAt()).isEqualTo(startedAt);
        assertThat(result.accumulatedStudySeconds()).isEqualTo(200);
        verify(repository).findActiveByMemberId(1L);
    }

    @Test
    void returnsNullWhenActiveSessionDoesNotExist() {
        when(repository.findActiveByMemberId(1L)).thenReturn(Optional.empty());

        GetCurrentStudyTimerSessionUseCase.CurrentStudyTimerSessionView result =
                service.handle(new GetCurrentStudyTimerSessionQuery(1L));

        assertThat(result).isNull();
        verify(repository).findActiveByMemberId(1L);
    }
}
