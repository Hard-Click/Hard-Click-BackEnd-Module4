package com.wanted.backend.domain.study_timer.infrastructure.persistence;

import com.wanted.backend.domain.study_timer.domain.model.DailyStudyStat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DailyStudyStatsRepositoryAdapterTest {

    private SpringDataDailyStudyStatsRepository repository;
    private DailyStudyStatsRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataDailyStudyStatsRepository.class);
        adapter = new DailyStudyStatsRepositoryAdapter(repository);
    }

    @Test
    void findsStatsByMemberIdAndDateRange() {
        LocalDate startDate = LocalDate.parse("2026-05-01");
        LocalDate endDate = LocalDate.parse("2026-05-03");
        LocalDateTime now = LocalDateTime.parse("2026-05-04T00:00:00");
        when(repository.findByMemberIdAndStudyDateBetweenOrderByStudyDateAsc(1L, startDate, endDate))
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
        verify(repository).findByMemberIdAndStudyDateBetweenOrderByStudyDateAsc(1L, startDate, endDate);
    }
}
