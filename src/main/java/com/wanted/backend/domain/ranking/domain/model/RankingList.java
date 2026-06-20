package com.wanted.backend.domain.ranking.domain.model;

import java.util.List;

public record RankingList(
        Long totalUsers,
        List<RankingEntry> entries
) {
    public static RankingList empty(Long totalUsers) {
        return new RankingList(totalUsers == null ? 0L : totalUsers, List.of());
    }
}
