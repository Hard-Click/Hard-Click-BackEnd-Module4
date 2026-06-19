package com.wanted.backend.domain.grass.infrastructure.persistence;

import com.wanted.backend.domain.grass.domain.model.StudyTimeGrassStat;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.DailyStudyStatsJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StudyTimeGrassRepositoryAdapterTest {

    private SpringDataGrassDailyStudyStatsRepository repository;
    private StudyTimeGrassRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataGrassDailyStudyStatsRepository.class);
        adapter = new StudyTimeGrassRepositoryAdapter(repository);
    }

    @Test
    void findsStudyTimeGrassStatsByDateRange() {
        LocalDate startDate = LocalDate.parse("2026-01-01");
        LocalDate endDate = LocalDate.parse("2026-01-03");
        LocalDateTime now = LocalDateTime.parse("2026-01-04T00:00:00");
        when(repository.findByMemberIdAndStatDateBetweenOrderByStatDateAsc(1L, startDate, endDate))
                .thenReturn(List.of(
                        new DailyStudyStatsJpaEntity(1L, LocalDate.parse("2026-01-01"), 0, 1200, 0, now, now),
                        new DailyStudyStatsJpaEntity(1L, LocalDate.parse("2026-01-03"), 0, 8000, 0, now, now)
                ));

        List<StudyTimeGrassStat> result = adapter.findByMemberIdAndDateBetween(1L, startDate, endDate);

        assertThat(result)
                .extracting(
                        StudyTimeGrassStat::memberId,
                        StudyTimeGrassStat::statDate,
                        StudyTimeGrassStat::studySeconds
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, LocalDate.parse("2026-01-01"), 1200),
                        org.assertj.core.groups.Tuple.tuple(1L, LocalDate.parse("2026-01-03"), 8000)
                );
    }
}
