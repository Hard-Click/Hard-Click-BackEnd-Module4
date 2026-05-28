package com.wanted.backend.domain.community.application.port;

import com.wanted.backend.domain.community.domain.model.BoardType;

import java.time.LocalDateTime;
import java.util.List;

public interface MyActivityQueryPort {

    MyActivityData findByMemberId(Long memberId);

    record MyActivityData(
            List<MyPostActivityData> posts,
            List<MyCommentActivityData> comments,
            List<MyReviewActivityData> reviews
    ) {
    }

    record MyPostActivityData(
            Long postId,
            BoardType boardType,
            String title,
            Integer viewCount,
            Boolean accepted,
            LocalDateTime createdAt
    ) {
    }

    record MyCommentActivityData(
            Long commentId,
            Long postId,
            Long parentId,
            String content,
            Boolean accepted,
            LocalDateTime createdAt
    ) {
    }

    record MyReviewActivityData(
            Long reviewId,
            Long courseId,
            Integer rating,
            String content,
            LocalDateTime createdAt
    ) {
    }
}
