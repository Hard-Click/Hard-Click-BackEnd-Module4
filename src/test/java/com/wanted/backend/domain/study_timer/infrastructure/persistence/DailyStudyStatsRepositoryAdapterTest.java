package com.wanted.backend.domain.study_timer.infrastructure.persistence;

import com.wanted.backend.domain.study_timer.domain.model.DailyStudyStat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DailyStudyStatsRepositoryAdapterTest {

    private SpringDataDailyStudyStatsRepository repository;
    private DailyStudyStatsRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataDailyStudyStatsRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T06:10:00Z"), ZoneId.of("Asia/Seoul"));
        adapter = new DailyStudyStatsRepositoryAdapter(repository, clock);
    }

    @Test
    void createsDailyStatsWhenUpsertingFirstStudySeconds() {
        LocalDate studyDate = LocalDate.parse("2026-05-11");
        when(repository.findByMemberIdAndStatDate(1L, studyDate)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(DailyStudyStatsJpaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DailyStudyStat result = adapter.upsertStudySeconds(1L, studyDate, 300);

        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.studyDate()).isEqualTo(studyDate);
        assertThat(result.studySeconds()).isEqualTo(300);
        verify(repository).findByMemberIdAndStatDate(1L, studyDate);
        verify(repository).saveAndFlush(any(DailyStudyStatsJpaEntity.class));
    }

    @Test
    void increasesDailyStatsWhenRowAlreadyExists() {
        LocalDate studyDate = LocalDate.parse("2026-05-11");
        LocalDateTime now = LocalDateTime.parse("2026-05-11T15:00:00");
        DailyStudyStatsJpaEntity entity = new DailyStudyStatsJpaEntity(1L, studyDate, 120, now, now);
        when(repository.findByMemberIdAndStatDate(1L, studyDate)).thenReturn(Optional.of(entity));
        when(repository.saveAndFlush(entity)).thenReturn(entity);

        DailyStudyStat result = adapter.upsertStudySeconds(1L, studyDate, 80);

        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.studyDate()).isEqualTo(studyDate);
        assertThat(result.studySeconds()).isEqualTo(200);
        verify(repository).findByMemberIdAndStatDate(1L, studyDate);
        verify(repository).saveAndFlush(entity);
    }

    @Test
    void findsStatsByMemberIdAndDateRange() {
        LocalDate startDate = LocalDate.parse("2026-05-01");
        LocalDate endDate = LocalDate.parse("2026-05-03");
        LocalDateTime now = LocalDateTime.parse("2026-05-04T00:00:00");
        when(repository.findByMemberIdAndStatDateBetweenOrderByStatDateAsc(1L, startDate, endDate))
                .thenReturn(List.of(
                        new DailyStudyStatsJpaEntity(1L, LocalDate.parse("2026-05-01"), 120, now, now),
                        new DailyStudyStatsJpaEntity(1L, LocalDate.parse("2026-05-03"), 300, now, now)
                ));

        List<DailyStudyStat> result = adapter.findByMemberIdAndDateBetween(1L, startDate, endDate);

        assertThat(result)
                .extracting(DailyStudyStat::memberId, DailyStudyStat::studyDate, DailyStudyStat::studySeconds)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, LocalDate.parse("2026-05-01"), 120),
                        org.assertj.core.groups.Tuple.tuple(1L, LocalDate.parse("2026-05-03"), 300)
                );
        verify(repository).findByMemberIdAndStatDateBetweenOrderByStatDateAsc(1L, startDate, endDate);
    }
}
