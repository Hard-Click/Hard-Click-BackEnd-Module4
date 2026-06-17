package com.wanted.backend.domain.study_timer.infrastructure.persistence;

import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StudyTimerSessionRepositoryAdapterTest {

    private SpringDataStudyTimerSessionRepository repository;
    private StudyTimerSessionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataStudyTimerSessionRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-06-16T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        adapter = new StudyTimerSessionRepositoryAdapter(repository, clock);
    }

    @Test
    void delegatesRunningSessionLookup() {
        when(repository.existsByMemberIdAndStatus(1L, StudyTimerSessionStatus.RUNNING)).thenReturn(true);

        assertThat(adapter.existsRunningByMemberId(1L)).isTrue();
    }

    @Test
    void savesStartedSessionWhenStorageAcceptsIt() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");

        when(repository.saveAndFlush(any(StudyTimerSessionJpaEntity.class)))
                .thenReturn(new StudyTimerSessionJpaEntity(
                        1L,
                        null,
                        null,
                        startedAt.toLocalDateTime(),
                        null,
                        0,
                        StudyTimerSessionStatus.RUNNING,
                        startedAt.toLocalDateTime(),
                        startedAt.toLocalDateTime()
                ));

        StudyTimerSession saved = adapter.save(StudyTimerSession.start(1L, startedAt));

        assertThat(saved.memberId()).isEqualTo(1L);
        assertThat(saved.status()).isEqualTo(StudyTimerSessionStatus.RUNNING);
        assertThat(saved.startedAt()).isEqualTo(startedAt);
    }
}
