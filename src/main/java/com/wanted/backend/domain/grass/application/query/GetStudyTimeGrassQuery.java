package com.wanted.backend.domain.grass.application.query;

public record GetStudyTimeGrassQuery(
        Long memberId,
        Integer year
) {
    public GetStudyTimeGrassQuery {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
    }
}
