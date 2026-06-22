package com.wanted.backend.domain.community.presentation.response;

import java.time.LocalDateTime;
import java.util.List;

public record StudyListResponse(
        List<StudyItem> content,
        int totalPages
) {
    public record StudyItem(
            Long groupId,
            String title,
            String content,
            String authorName,
            String subjectName,
            int currentCount,
            int maxCount,
            boolean isClosed,
            LocalDateTime createdAt
    ) {}
}