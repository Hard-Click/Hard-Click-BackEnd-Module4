package com.wanted.backend.domain.grass.infrastructure.persistence;

import com.wanted.backend.domain.grass.domain.model.DailyGrassDetailStat;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.DailyStudyStatsJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DailyGrassDetailRepositoryAdapterTest {

    private SpringDataGrassDailyStudyStatsRepository repository;
    private DailyGrassDetailRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataGrassDailyStudyStatsRepository.class);
        adapter = new DailyGrassDetailRepositoryAdapter(repository);
    }

    @Test
    void findsDailyGrassDetailByDate() {
        LocalDate date = LocalDate.parse("2026-06-18");
        LocalDateTime now = LocalDateTime.parse("2026-06-18T00:00:00");
        when(repository.findByMemberIdAndStatDate(1L, date))
                .thenReturn(Optional.of(
                        new DailyStudyStatsJpaEntity(1L, date, 3, 5400, 1, now, now)
                ));

        Optional<DailyGrassDetailStat> result = adapter.findByMemberIdAndStatDate(1L, date);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().memberId()).isEqualTo(1L);
        assertThat(result.orElseThrow().statDate()).isEqualTo(date);
        assertThat(result.orElseThrow().watchedLessonCount()).isEqualTo(3);
        assertThat(result.orElseThrow().studySeconds()).isEqualTo(5400);
        verify(repository).findByMemberIdAndStatDate(1L, date);
    }
}
