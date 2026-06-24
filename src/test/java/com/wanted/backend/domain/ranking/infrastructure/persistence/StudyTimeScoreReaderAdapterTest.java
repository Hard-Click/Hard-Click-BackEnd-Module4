package com.wanted.backend.domain.ranking.infrastructure.persistence;

import com.wanted.backend.domain.study_timer.infrastructure.persistence.DailyStudyStatsJpaEntity;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.SpringDataDailyStudyStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudyTimeScoreReaderAdapterTest {

    private SpringDataDailyStudyStatsRepository repository;
    private StudyTimeScoreReaderAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataDailyStudyStatsRepository.class);
        adapter = new StudyTimeScoreReaderAdapter(repository);
    }

    @Test
    void sumsStudySecondsByMemberWithinDateRange() {
        LocalDate startDate = LocalDate.parse("2026-05-01");
        LocalDate endDate = LocalDate.parse("2026-05-03");
        LocalDateTime now = LocalDateTime.parse("2026-05-04T00:00:00");
        when(repository.findByStatDateBetween(startDate, endDate))
                .thenReturn(List.of(
                        new DailyStudyStatsJpaEntity(1L, LocalDate.parse("2026-05-01"), 120, now, now),
                        new DailyStudyStatsJpaEntity(1L, LocalDate.parse("2026-05-03"), 300, now, now),
                        new DailyStudyStatsJpaEntity(2L, LocalDate.parse("2026-05-03"), 600, now, now)
                ));

        Map<Long, Long> result = adapter.sumStudySecondsByDateBetween(startDate, endDate);

        assertThat(result).containsEntry(1L, 420L);
        assertThat(result).containsEntry(2L, 600L);
        verify(repository).findByStatDateBetween(startDate, endDate);
    }
}
