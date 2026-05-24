package com.wanted.backend.domain.community.presentation.response;

import com.wanted.backend.domain.community.domain.model.BoardType;
import java.time.LocalDateTime;

public record PostItemResponse(
        Long postId,
        BoardType boardType,
        String title,
        String authorName,
        LocalDateTime createdAt,
        int viewCount,
        int commentCount
) {}