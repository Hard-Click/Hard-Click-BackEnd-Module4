package com.wanted.backend.domain.grass.application.query;

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
    }
}
