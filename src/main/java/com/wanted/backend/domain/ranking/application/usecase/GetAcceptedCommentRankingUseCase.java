package com.wanted.backend.domain.ranking.application.usecase;

import com.wanted.backend.domain.ranking.application.query.GetAcceptedCommentRankingQuery;

import java.util.List;

public interface GetAcceptedCommentRankingUseCase {

    AcceptedCommentRankingView handle(GetAcceptedCommentRankingQuery query);

    record AcceptedCommentRankingView(
            String period,
            Long totalUsers,
            List<AcceptedCommentRankingItem> rankings
    ) {
    }

    record AcceptedCommentRankingItem(
            Long rank,
            Long memberId,
            Long acceptedCommentCount
    ) {
    }
}
