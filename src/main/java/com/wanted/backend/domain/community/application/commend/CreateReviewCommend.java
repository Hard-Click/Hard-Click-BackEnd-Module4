package com.wanted.backend.domain.community.application.commend;

public record CreateReviewCommend(
        Long memberId,
        Long courseId,
        Double rating,
        String content
) {
}
