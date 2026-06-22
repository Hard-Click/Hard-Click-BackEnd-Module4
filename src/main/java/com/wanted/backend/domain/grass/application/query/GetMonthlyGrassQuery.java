package com.wanted.backend.domain.grass.application.query;

public record GetMonthlyGrassQuery(
        Long memberId,
        Integer year,
        Integer month
) {
}
