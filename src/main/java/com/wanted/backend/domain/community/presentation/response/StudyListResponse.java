package com.wanted.backend.domain.community.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;

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
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss+09:00")
            LocalDateTime createdAt
    ) {}
}