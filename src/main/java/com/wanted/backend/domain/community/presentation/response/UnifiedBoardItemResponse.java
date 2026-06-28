package com.wanted.backend.domain.community.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wanted.backend.domain.community.application.result.PostItemResult;

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
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss+09:00")
        LocalDateTime createdAt
) {
    public static UnifiedBoardItemResponse fromPost(PostItemResponse post) {
        return new UnifiedBoardItemResponse(
                "POST", post.postId(), null, post.boardType().name(),
                post.title(), post.authorName(), post.viewCount(), post.commentCount(),
                null, null, null, null, post.createdAt()
        );
    }

    public static UnifiedBoardItemResponse fromPostItem(PostItemResult result) {
        return new UnifiedBoardItemResponse(
                "POST", result.postId(), null, result.boardType().name(),
                result.title(), result.authorName(), result.viewCount(), result.commentCount(),
                null, null, null, null, result.createdAt()
        );
    }
}