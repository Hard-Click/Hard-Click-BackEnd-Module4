package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingScoreWriter;
import com.wanted.backend.domain.ranking.application.port.StudyTimeScoreReader;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingRebuildScheduler {

    private final StudyTimeScoreReader studyTimeScoreReader;
    private final RankingScoreWriter rankingScoreWriter;
    private final Clock clock;

    @Scheduled(cron = "${ranking.study-time-rebuild.cron:0 10 0 * * *}")
    public void rebuildStudyTimeScores() {
        LocalDate today = LocalDate.now(clock);

        rebuildPeriod(RankingPeriod.DAILY, today, today);
        rebuildPeriod(
                RankingPeriod.WEEKLY,
                today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                today
        );
        rebuildPeriod(
                RankingPeriod.MONTHLY,
                today.with(TemporalAdjusters.firstDayOfMonth()),
                today
        );
    }

    private void rebuildPeriod(RankingPeriod period, LocalDate startDate, LocalDate endDate) {
        try {
            Map<Long, Long> scores = studyTimeScoreReader.sumStudySecondsByDateBetween(startDate, endDate);
            rankingScoreWriter.replaceScores(RankingMetric.STUDY_TIME, period, scores);
        } catch (Exception exception) {
            log.error(
                    "[Ranking] study-time ranking rebuild failed. period={}, startDate={}, endDate={}",
                    period.value(),
                    startDate,
                    endDate,
                    exception
            );
        }
    }
}
