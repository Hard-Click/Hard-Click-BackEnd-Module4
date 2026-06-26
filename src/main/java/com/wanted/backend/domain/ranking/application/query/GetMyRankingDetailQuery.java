package com.wanted.backend.domain.ranking.application.query;

public record GetMyRankingDetailQuery(
        Long memberId,
        String metric,
        String period
) {
}
