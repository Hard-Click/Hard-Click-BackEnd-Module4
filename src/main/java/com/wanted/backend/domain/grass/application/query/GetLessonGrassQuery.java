package com.wanted.backend.domain.grass.application.query;

public record GetLessonGrassQuery(
        Long memberId
) {
    public GetLessonGrassQuery {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
    }
}
