package com.wanted.backend.domain.community.application.result;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResult(
        Long commentId,
        String authorName,
        String authorInitial,
        String content,
        LocalDateTime createdAt,
        boolean isAccepted,
        boolean isMine,
        boolean isDeleted,
        String imageUrl,
        List<CommentResult> replies
) {}