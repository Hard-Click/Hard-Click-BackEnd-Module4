package com.wanted.backend.domain.study_timer.infrastructure.persistence;

import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

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
    void findsSessionById() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        StudyTimerSessionJpaEntity entity = new StudyTimerSessionJpaEntity(
                1L,
                null,
                null,
                startedAt.toLocalDateTime(),
                null,
                0,
                StudyTimerSessionStatus.RUNNING,
                startedAt.toLocalDateTime(),
                startedAt.toLocalDateTime()
        );
        ReflectionTestUtils.setField(entity, "id", 55L);

        when(repository.findById(55L)).thenReturn(Optional.of(entity));

        Optional<StudyTimerSession> found = adapter.findById(55L);

        assertThat(found).isPresent();
        assertThat(found.orElseThrow().id()).isEqualTo(55L);
        assertThat(found.orElseThrow().memberId()).isEqualTo(1L);
    }

    @Test
    void savesStartedSessionWhenStorageAcceptsIt() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        StudyTimerSessionJpaEntity savedEntity = new StudyTimerSessionJpaEntity(
                1L,
                null,
                null,
                startedAt.toLocalDateTime(),
                null,
                0,
                StudyTimerSessionStatus.RUNNING,
                startedAt.toLocalDateTime(),
                startedAt.toLocalDateTime()
        );
        ReflectionTestUtils.setField(savedEntity, "id", 55L);

        when(repository.saveAndFlush(any(StudyTimerSessionJpaEntity.class))).thenReturn(savedEntity);

        StudyTimerSession saved = adapter.save(StudyTimerSession.start(1L, startedAt));

        assertThat(saved.id()).isEqualTo(55L);
        assertThat(saved.memberId()).isEqualTo(1L);
        assertThat(saved.status()).isEqualTo(StudyTimerSessionStatus.RUNNING);
        assertThat(saved.startedAt()).isEqualTo(startedAt);
    }

    @Test
    void updatesExistingSessionWhenHeartbeatIsSaved() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        StudyTimerSessionJpaEntity entity = new StudyTimerSessionJpaEntity(
                1L,
                null,
                null,
                startedAt.toLocalDateTime(),
                null,
                0,
                StudyTimerSessionStatus.RUNNING,
                startedAt.toLocalDateTime(),
                startedAt.toLocalDateTime()
        );
        ReflectionTestUtils.setField(entity, "id", 55L);

        when(repository.findById(55L)).thenReturn(Optional.of(entity));
        when(repository.saveAndFlush(entity)).thenReturn(entity);

        StudyTimerSession saved = adapter.save(new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                startedAt,
                null,
                200,
                StudyTimerSessionStatus.RUNNING
        ));

        assertThat(saved.id()).isEqualTo(55L);
        assertThat(saved.accumulatedStudySeconds()).isEqualTo(200);
        assertThat(saved.status()).isEqualTo(StudyTimerSessionStatus.RUNNING);
    }

    @Test
    void updatesExistingSessionWhenEndIsSaved() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-05-11T15:00:00+09:00");
        OffsetDateTime endedAt = OffsetDateTime.parse("2026-05-11T15:08:20+09:00");
        StudyTimerSessionJpaEntity entity = new StudyTimerSessionJpaEntity(
                1L,
                null,
                null,
                startedAt.toLocalDateTime(),
                null,
                200,
                StudyTimerSessionStatus.RUNNING,
                startedAt.toLocalDateTime(),
                startedAt.toLocalDateTime()
        );
        ReflectionTestUtils.setField(entity, "id", 55L);

        when(repository.findById(55L)).thenReturn(Optional.of(entity));
        when(repository.saveAndFlush(entity)).thenReturn(entity);

        StudyTimerSession saved = adapter.save(new StudyTimerSession(
                55L,
                1L,
                null,
                null,
                startedAt,
                endedAt,
                500,
                StudyTimerSessionStatus.ENDED
        ));

        assertThat(saved.id()).isEqualTo(55L);
        assertThat(saved.endedAt()).isEqualTo(endedAt);
        assertThat(saved.accumulatedStudySeconds()).isEqualTo(500);
        assertThat(saved.status()).isEqualTo(StudyTimerSessionStatus.ENDED);
    }
}
