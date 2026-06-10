package com.wanted.backend.domain.community.application.command;

public record UpdateReviewCommand(
        Long memberId,
        Long reviewId,
        Integer rating,
        String content
) {

}