package com.wanted.backend.domain.community.presentation.response;

import com.wanted.backend.domain.community.domain.model.BoardType;
import java.time.LocalDateTime;
import java.util.List;


public record PostDetailResponse(
        Long postId,
        BoardType boardType,
        String title,
        String authorName,
        LocalDateTime createdAt,
        int viewCount,
        String content,
        boolean isMyPost,
        boolean isAccepted,
        List<String> fileUrls
) {}