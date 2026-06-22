package com.wanted.backend.domain.ranking.domain.model;

public record RankingEntry(
        Long rank,
        Long memberId,
        Long score
) {
}
