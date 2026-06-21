package com.wanted.backend.domain.ranking.domain.model;

public record RankingSummary(
        Long rank,
        Long totalUsers
) {
    public static RankingSummary empty() {
        return new RankingSummary(null, 0L);
    }

    public static RankingSummary notRanked(Long totalUsers) {
        return new RankingSummary(null, totalUsers == null ? 0L : totalUsers);
    }
}
