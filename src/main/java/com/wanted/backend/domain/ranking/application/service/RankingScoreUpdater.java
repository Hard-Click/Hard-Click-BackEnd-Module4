package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingScoreWriter;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import com.wanted.backend.domain.study_timer.domain.event.StudySessionEndedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingScoreUpdater {

    private final RankingScoreWriter rankingScoreWriter;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(StudySessionEndedEvent event) {
        if (event.deltaStudySeconds() == null || event.deltaStudySeconds() <= 0) {
            return;
        }

        for (RankingPeriod period : RankingPeriod.values()) {
            incrementStudyTimeScore(event, period);
        }
    }

    private void incrementStudyTimeScore(StudySessionEndedEvent event, RankingPeriod period) {
        try {
            rankingScoreWriter.incrementScore(
                    RankingMetric.STUDY_TIME,
                    period,
                    event.memberId(),
                    event.deltaStudySeconds()
            );
        } catch (Exception exception) {
            log.error(
                    "[Ranking] study-time score increment failed. memberId={}, period={}, deltaStudySeconds={}",
                    event.memberId(),
                    period.value(),
                    event.deltaStudySeconds(),
                    exception
            );
        }
    }
}
