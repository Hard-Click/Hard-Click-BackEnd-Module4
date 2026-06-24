package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingScoreWriter;
import com.wanted.backend.domain.ranking.application.port.StudyTimeScoreReader;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RankingRebuildSchedulerTest {

    private StudyTimeScoreReader studyTimeScoreReader;
    private RankingScoreWriter rankingScoreWriter;
    private RankingRebuildScheduler scheduler;

    @BeforeEach
    void setUp() {
        studyTimeScoreReader = mock(StudyTimeScoreReader.class);
        rankingScoreWriter = mock(RankingScoreWriter.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-13T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        scheduler = new RankingRebuildScheduler(studyTimeScoreReader, rankingScoreWriter, clock);
    }

    @Test
    void rebuildsDailyWeeklyAndMonthlyStudyTimeScores() {
        LocalDate today = LocalDate.parse("2026-05-13");
        LocalDate weekStart = LocalDate.parse("2026-05-11");
        LocalDate monthStart = LocalDate.parse("2026-05-01");
        Map<Long, Long> dailyScores = Map.of(1L, 300L);
        Map<Long, Long> weeklyScores = Map.of(1L, 300L, 2L, 600L);
        Map<Long, Long> monthlyScores = Map.of(1L, 900L, 2L, 600L);
        when(studyTimeScoreReader.sumStudySecondsByDateBetween(today, today)).thenReturn(dailyScores);
        when(studyTimeScoreReader.sumStudySecondsByDateBetween(weekStart, today)).thenReturn(weeklyScores);
        when(studyTimeScoreReader.sumStudySecondsByDateBetween(monthStart, today)).thenReturn(monthlyScores);

        scheduler.rebuildStudyTimeScores();

        verify(rankingScoreWriter).replaceScores(RankingMetric.STUDY_TIME, RankingPeriod.DAILY, dailyScores);
        verify(rankingScoreWriter).replaceScores(RankingMetric.STUDY_TIME, RankingPeriod.WEEKLY, weeklyScores);
        verify(rankingScoreWriter).replaceScores(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY, monthlyScores);
    }

    @Test
    void keepsRebuildingOtherPeriodsWhenOnePeriodFails() {
        LocalDate today = LocalDate.parse("2026-05-13");
        LocalDate weekStart = LocalDate.parse("2026-05-11");
        LocalDate monthStart = LocalDate.parse("2026-05-01");
        Map<Long, Long> weeklyScores = Map.of(1L, 300L);
        Map<Long, Long> monthlyScores = Map.of(1L, 900L);
        doThrow(new RuntimeException("rdb down"))
                .when(studyTimeScoreReader)
                .sumStudySecondsByDateBetween(today, today);
        when(studyTimeScoreReader.sumStudySecondsByDateBetween(weekStart, today)).thenReturn(weeklyScores);
        when(studyTimeScoreReader.sumStudySecondsByDateBetween(monthStart, today)).thenReturn(monthlyScores);

        scheduler.rebuildStudyTimeScores();

        verify(rankingScoreWriter).replaceScores(RankingMetric.STUDY_TIME, RankingPeriod.WEEKLY, weeklyScores);
        verify(rankingScoreWriter).replaceScores(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY, monthlyScores);
    }

    @Test
    void keepsRebuildingOtherPeriodsWhenReplaceScoresFailsForOnePeriod() {
        LocalDate today = LocalDate.parse("2026-05-13");
        LocalDate weekStart = LocalDate.parse("2026-05-11");
        LocalDate monthStart = LocalDate.parse("2026-05-01");
        Map<Long, Long> dailyScores = Map.of(1L, 300L);
        Map<Long, Long> weeklyScores = Map.of(1L, 300L);
        Map<Long, Long> monthlyScores = Map.of(1L, 900L);
        when(studyTimeScoreReader.sumStudySecondsByDateBetween(today, today)).thenReturn(dailyScores);
        when(studyTimeScoreReader.sumStudySecondsByDateBetween(weekStart, today)).thenReturn(weeklyScores);
        when(studyTimeScoreReader.sumStudySecondsByDateBetween(monthStart, today)).thenReturn(monthlyScores);
        doThrow(new RuntimeException("redis down"))
                .when(rankingScoreWriter)
                .replaceScores(RankingMetric.STUDY_TIME, RankingPeriod.DAILY, dailyScores);

        scheduler.rebuildStudyTimeScores();

        verify(rankingScoreWriter).replaceScores(RankingMetric.STUDY_TIME, RankingPeriod.DAILY, dailyScores);
        verify(rankingScoreWriter).replaceScores(RankingMetric.STUDY_TIME, RankingPeriod.WEEKLY, weeklyScores);
        verify(rankingScoreWriter).replaceScores(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY, monthlyScores);
    }
}
