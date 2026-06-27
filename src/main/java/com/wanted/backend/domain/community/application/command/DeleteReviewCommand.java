package com.wanted.backend.domain.community.application.command;

public record DeleteReviewCommand(
        Long memberId,
        Long reviewId,
        boolean isAdmin
){
}