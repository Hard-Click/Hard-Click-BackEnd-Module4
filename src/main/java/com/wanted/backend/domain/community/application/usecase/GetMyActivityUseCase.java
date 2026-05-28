package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.domain.model.BoardType;

import java.time.LocalDateTime;
import java.util.List;

public interface GetMyActivityUseCase {

    MyActivityView handle(Long memberId);

    record MyActivityView(
            List<MyPostActivity> posts,
            List<MyCommentActivity> comments,
            List<MyReviewActivity> reviews
    ) {
    }

    record MyPostActivity(
            Long postId,
            BoardType boardType,
            String title,
            Integer viewCount,
            Boolean accepted,
            LocalDateTime createdAt
    ) {
    }

    record MyCommentActivity(
            Long commentId,
            Long postId,
            Long parentId,
            String content,
            Boolean accepted,
            LocalDateTime createdAt
    ) {
    }

    record MyReviewActivity(
            Long reviewId,
            Long courseId,
            Integer rating,
            String content,
            LocalDateTime createdAt
    ) {
    }
}
