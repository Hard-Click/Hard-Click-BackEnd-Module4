package com.wanted.backend.domain.stats.infrastructure.persistence;

import com.wanted.backend.domain.stats.domain.model.DailyStudyStat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

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
    void findsStatByMemberIdAndStatDate() {
        LocalDate date = LocalDate.parse("2026-06-18");
        LocalDateTime now = LocalDateTime.parse("2026-06-18T23:59:59");
        when(repository.findByMemberIdAndStatDate(1L, date))
                .thenReturn(Optional.of(new DailyStudyStatsJpaEntity(
                        1L,
                        date,
                        3,
                        9000,
                        2,
                        now,
                        now
                )));

        Optional<DailyStudyStat> result = adapter.findByMemberIdAndStatDate(1L, date);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().memberId()).isEqualTo(1L);
        assertThat(result.orElseThrow().statDate()).isEqualTo(date);
        assertThat(result.orElseThrow().watchedLessonCount()).isEqualTo(3);
        assertThat(result.orElseThrow().studySeconds()).isEqualTo(9000);
        assertThat(result.orElseThrow().completedLessonCount()).isEqualTo(2);
        verify(repository).findByMemberIdAndStatDate(1L, date);
    }
}
