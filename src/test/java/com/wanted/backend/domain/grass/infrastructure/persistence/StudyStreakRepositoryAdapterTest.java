package com.wanted.backend.domain.grass.infrastructure.persistence;

import com.wanted.backend.domain.grass.domain.model.StudyStreakStat;
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

class StudyStreakRepositoryAdapterTest {

    private SpringDataGrassDailyStudyStatsRepository repository;
    private StudyStreakRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataGrassDailyStudyStatsRepository.class);
        adapter = new StudyStreakRepositoryAdapter(repository);
    }

    @Test
    void findsStatsBeforeTodayForStudyStreak() {
        LocalDate today = LocalDate.parse("2026-06-20");
        LocalDateTime now = LocalDateTime.parse("2026-06-20T00:00:00");
        when(repository.findByMemberIdAndStatDateLessThanEqualOrderByStatDateDesc(1L, today))
                .thenReturn(List.of(
                        new DailyStudyStatsJpaEntity(1L, today, 1, 3600, 0, now, now),
                        new DailyStudyStatsJpaEntity(1L, LocalDate.parse("2026-06-19"), 0, 1800, 0, now, now)
                ));

        List<StudyStreakStat> result = adapter.findByMemberIdAndDateLessThanEqual(1L, today);

        assertThat(result)
                .extracting(
                        StudyStreakStat::statDate,
                        StudyStreakStat::watchedLessonCount,
                        StudyStreakStat::studySeconds
                )
                .containsExactly(
                        tuple(today, 1, 3600),
                        tuple(LocalDate.parse("2026-06-19"), 0, 1800)
                );
        verify(repository).findByMemberIdAndStatDateLessThanEqualOrderByStatDateDesc(1L, today);
    }
}
