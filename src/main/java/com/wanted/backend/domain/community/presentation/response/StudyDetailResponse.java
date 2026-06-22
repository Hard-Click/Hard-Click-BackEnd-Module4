package com.wanted.backend.domain.community.presentation.response;

import java.time.LocalDateTime;
import java.util.List;

public record StudyDetailResponse(
        Long groupId,
        String title,
        String content,
        String subjectName,
        String authorName,
        int currentCount,
        int maxCount,
        boolean isMine,
        boolean isJoined,
        boolean isClosed,
        List<String> members,
        LocalDateTime createdAt
) {}