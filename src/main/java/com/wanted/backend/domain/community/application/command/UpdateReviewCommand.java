package com.wanted.backend.domain.community.application.command;

public record UpdateReviewCommand(
        Long memberId,    // 본인 검증용
        Long reviewId,    // 수정할 리뷰 ID
        Integer rating,   // 수정할 별점
        String content    // 수정할 내용
) {}