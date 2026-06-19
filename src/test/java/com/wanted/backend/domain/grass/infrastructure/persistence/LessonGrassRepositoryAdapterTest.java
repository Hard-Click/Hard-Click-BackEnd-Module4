package com.wanted.backend.domain.grass.infrastructure.persistence;

import com.wanted.backend.domain.grass.domain.model.LessonGrassStat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LessonGrassRepositoryAdapterTest {

    private SpringDataLessonGrassStatsRepository repository;
    private LessonGrassRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataLessonGrassStatsRepository.class);
        adapter = new LessonGrassRepositoryAdapter(repository);
    }

    @Test
    void findsLessonGrassStatsByDateRange() {
        LocalDate startDate = LocalDate.parse("2026-01-01");
        LocalDate endDate = LocalDate.parse("2026-01-03");
        when(repository.findByMemberIdAndStatDateBetweenOrderByStatDateAsc(1L, startDate, endDate))
                .thenReturn(List.of(
                        new LessonGrassStatsJpaEntity(1L, LocalDate.parse("2026-01-01"), 1),
                        new LessonGrassStatsJpaEntity(1L, LocalDate.parse("2026-01-03"), 5)
                ));

        List<LessonGrassStat> result = adapter.findByMemberIdAndDateBetween(1L, startDate, endDate);

        assertThat(result)
                .extracting(
                        LessonGrassStat::memberId,
                        LessonGrassStat::statDate,
                        LessonGrassStat::watchedLessonCount
                )
                .containsExactly(
                        tuple(1L, LocalDate.parse("2026-01-01"), 1),
                        tuple(1L, LocalDate.parse("2026-01-03"), 5)
                );
        verify(repository).findByMemberIdAndStatDateBetweenOrderByStatDateAsc(1L, startDate, endDate);
    }
}
