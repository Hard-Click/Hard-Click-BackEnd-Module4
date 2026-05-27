package com.wanted.backend.domain.community.presentation.response;

import java.time.LocalDateTime;
import java.util.List;


public record CommentResponse(
        Long commentId,
        String authorName,
        String authorInitial,
        String content,
        LocalDateTime createdAt,
        boolean isAccepted,
        boolean isMine,
        boolean isDeleted,
        String imageUrl,
        List<CommentResponse> replies  // 대댓글 목록
) {

}