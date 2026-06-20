package com.wanted.backend.domain.ranking.domain.model;

import java.util.List;

public record RankingList(
        Long totalUsers,
        List<RankingEntry> entries
) {
    public RankingList {
        totalUsers = totalUsers == null ? 0L : totalUsers;
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public static RankingList empty(Long totalUsers) {
        return new RankingList(totalUsers, List.of());
    }
}
