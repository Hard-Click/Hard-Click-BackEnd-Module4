package com.wanted.backend.domain.community.presentation.response;

import java.time.LocalDateTime;

public record UnifiedBoardItemResponse(
        String type,
        Long postId,
        Long groupId,
        String boardType,
        String title,
        String authorName,
        Integer viewCount,
        Integer commentCount,
        String subjectName,
        Integer currentCount,
        Integer maxCount,
        Boolean isClosed,
        LocalDateTime createdAt
) {
    public static UnifiedBoardItemResponse fromPost(PostItemResponse post) {
        return new UnifiedBoardItemResponse(
                "POST", post.postId(), null, post.boardType().name(),
                post.title(), post.authorName(), post.viewCount(), post.commentCount(),
                null, null, null, null, post.createdAt()
        );
    }
}