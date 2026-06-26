package com.wanted.backend.domain.community.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;

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
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss+09:00")
        LocalDateTime createdAt
) {}