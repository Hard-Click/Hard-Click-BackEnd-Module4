package com.wanted.backend.domain.grass.infrastructure.persistence;

import com.wanted.backend.domain.grass.domain.model.YearlyGrassStat;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.DailyStudyStatsJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class YearlyGrassRepositoryAdapterTest {

    private SpringDataGrassDailyStudyStatsRepository repository;
    private YearlyGrassRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataGrassDailyStudyStatsRepository.class);
        adapter = new YearlyGrassRepositoryAdapter(repository);
    }

    @Test
    void findsYearlyGrassStatsByDateRange() {
        LocalDate startDate = LocalDate.parse("2026-01-01");
        LocalDate endDate = LocalDate.parse("2026-01-03");
        LocalDateTime now = LocalDateTime.parse("2026-01-04T00:00:00");
        when(repository.findByMemberIdAndStatDateBetweenOrderByStatDateAsc(1L, startDate, endDate))
                .thenReturn(List.of(
                        new DailyStudyStatsJpaEntity(1L, LocalDate.parse("2026-01-01"), 1, 1200, 0, now, now),
                        new DailyStudyStatsJpaEntity(1L, LocalDate.parse("2026-01-03"), 5, 8000, 0, now, now)
                ));

        List<YearlyGrassStat> result = adapter.findByMemberIdAndDateBetween(1L, startDate, endDate);

        assertThat(result)
                .extracting(
                        YearlyGrassStat::memberId,
                        YearlyGrassStat::statDate,
                        YearlyGrassStat::watchedLessonCount
                )
                .containsExactly(
                        tuple(1L, LocalDate.parse("2026-01-01"), 1),
                        tuple(1L, LocalDate.parse("2026-01-03"), 5)
                );
        verify(repository).findByMemberIdAndStatDateBetweenOrderByStatDateAsc(1L, startDate, endDate);
    }
}
