package com.wanted.backend.domain.community.application.command;

public record CreateReviewCommand(
        Long memberId,
        Long courseId,
        Double rating,
        String content
) {
}
