package com.wanted.backend.domain.ranking.domain.model;

public record RankingDetail(
        Long rank,
        Long totalUsers
) {
    public static RankingDetail empty() {
        return new RankingDetail(null, 0L);
    }

    public static RankingDetail notRanked(Long totalUsers) {
        return new RankingDetail(null, totalUsers == null ? 0L : totalUsers);
    }
}
