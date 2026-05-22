package com.wanted.backend.domain.community.application.command;

public record CreateReviewCommand(
        Long memberId,
        Long courseId,
        Integer rating,
        String content
) {
}
