package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.MemberNamePort;
import com.wanted.backend.domain.ranking.application.port.MemberStreakPort;
import com.wanted.backend.domain.ranking.application.port.RankingListReader;
import com.wanted.backend.domain.ranking.application.query.GetLessonRankingQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetLessonRankingUseCase;
import com.wanted.backend.domain.ranking.domain.model.RankingEntry;
import com.wanted.backend.domain.ranking.domain.model.RankingList;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import com.wanted.backend.domain.ranking.domain.policy.RankingPeriodPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLessonRankingService implements GetLessonRankingUseCase {

    private final RankingListReader rankingListReader;
    private final RankingPeriodPolicy rankingPeriodPolicy;
    private final MemberNamePort memberNamePort;
    private final MemberStreakPort memberStreakPort;

    @Override
    public LessonRankingView handle(GetLessonRankingQuery query) {
        RankingPeriod period = rankingPeriodPolicy.resolve(query.period());
        RankingList rankingList = rankingListReader.findByMetricAndPeriod(
                RankingMetric.LESSON,
                period
        );

        List<RankingEntry> entries = rankingList.entries();
        Map<Long, String> namesByMemberId = memberNamePort.getNamesByMemberIds(
                entries.stream().map(RankingEntry::memberId).toList()
        );

        return new LessonRankingView(
                period.value(),
                rankingList.totalUsers(),
                entries.stream()
                        .map(entry -> new LessonRankingItem(
                                entry.rank(),
                                entry.memberId(),
                                namesByMemberId.get(entry.memberId()),
                                entry.score(),
                                currentStreakDaysOrNull(entry.memberId())
                        ))
                        .toList()
        );
    }

    private Integer currentStreakDaysOrNull(Long memberId) {
        try {
            return memberStreakPort.getCurrentStreakDays(memberId);
        } catch (RuntimeException exception) {
            log.warn("[Ranking] 연속 학습일 조회 실패. memberId={}", memberId, exception);
            return null;
        }
    }
}
