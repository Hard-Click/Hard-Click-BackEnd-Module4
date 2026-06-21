package com.wanted.backend.domain.grass.application.query;

import java.time.Year;

public record GetYearlyGrassQuery(
        Long memberId,
        Integer year
) {
    public GetYearlyGrassQuery {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        if (year == null) {
            throw new IllegalArgumentException("조회 연도는 필수입니다.");
        }
        if (year < 1) {
            throw new IllegalArgumentException("조회 연도는 1 이상이어야 합니다.");
        }
        if (year > Year.MAX_VALUE) {
            throw new IllegalArgumentException("조회 연도 범위를 초과했습니다.");
        }
    }
}
