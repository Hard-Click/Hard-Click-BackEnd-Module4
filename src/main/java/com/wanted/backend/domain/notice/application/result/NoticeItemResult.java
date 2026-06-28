package com.wanted.backend.domain.notice.application.result;

import java.time.LocalDateTime;

public record NoticeItemResult(
        Long noticeId,
        String noticeType,
        String courseName,
        String title,
        boolean isPinned,
        boolean isRead,
        LocalDateTime createdAt
) {}