package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.MemberNamePort;
import com.wanted.backend.domain.ranking.application.port.MemberStreakPort;
import com.wanted.backend.domain.ranking.application.port.RankingListReader;
import com.wanted.backend.domain.ranking.application.query.GetAcceptedCommentRankingQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetAcceptedCommentRankingUseCase;
import com.wanted.backend.domain.ranking.domain.model.RankingEntry;
import com.wanted.backend.domain.ranking.domain.model.RankingList;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import com.wanted.backend.domain.ranking.domain.policy.RankingPeriodPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAcceptedCommentRankingService implements GetAcceptedCommentRankingUseCase {

    private final RankingListReader rankingListReader;
    private final RankingPeriodPolicy rankingPeriodPolicy;
    private final MemberNamePort memberNamePort;
    private final MemberStreakPort memberStreakPort;

    @Override
    public AcceptedCommentRankingView handle(GetAcceptedCommentRankingQuery query) {
        RankingPeriod period = rankingPeriodPolicy.resolve(query.period());
        RankingList rankingList = rankingListReader.findByMetricAndPeriod(
                RankingMetric.ACCEPTED_COMMENT,
                period
        );

        List<RankingEntry> entries = rankingList.entries();
        Map<Long, String> namesByMemberId = memberNamePort.getNamesByMemberIds(
                entries.stream().map(RankingEntry::memberId).toList()
        );

        return new AcceptedCommentRankingView(
                period.value(),
                rankingList.totalUsers(),
                entries.stream()
                        .map(entry -> new AcceptedCommentRankingItem(
                                entry.rank(),
                                entry.memberId(),
                                namesByMemberId.get(entry.memberId()),
                                entry.score(),
                                memberStreakPort.getCurrentStreakDays(entry.memberId())
                        ))
                        .toList()
        );
    }
}
