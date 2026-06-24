package com.wanted.backend.domain.ranking.infrastructure.persistence;

import com.wanted.backend.domain.study_timer.infrastructure.persistence.MemberStudySecondsSum;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.SpringDataDailyStudyStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
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
        when(repository.sumStudySecondsByDateBetween(startDate, endDate))
                .thenReturn(List.of(
                        sum(1L, 420L),
                        sum(2L, 600L)
                ));

        Map<Long, Long> result = adapter.sumStudySecondsByDateBetween(startDate, endDate);

        assertThat(result).containsEntry(1L, 420L);
        assertThat(result).containsEntry(2L, 600L);
        verify(repository).sumStudySecondsByDateBetween(startDate, endDate);
    }

    @Test
    void returnsEmptyMapWhenNoRowsMatchDateRange() {
        LocalDate startDate = LocalDate.parse("2026-05-01");
        LocalDate endDate = LocalDate.parse("2026-05-03");
        when(repository.sumStudySecondsByDateBetween(startDate, endDate)).thenReturn(List.of());

        Map<Long, Long> result = adapter.sumStudySecondsByDateBetween(startDate, endDate);

        assertThat(result).isEmpty();
    }

    private MemberStudySecondsSum sum(Long memberId, Long totalSeconds) {
        return new MemberStudySecondsSum() {
            @Override
            public Long getMemberId() {
                return memberId;
            }

            @Override
            public Long getTotalSeconds() {
                return totalSeconds;
            }
        };
    }
}
