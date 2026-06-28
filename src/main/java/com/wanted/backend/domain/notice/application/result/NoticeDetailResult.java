package com.wanted.backend.domain.notice.application.result;

import java.time.LocalDateTime;

public record NoticeDetailResult(
        Long noticeId,
        String noticeType,
        String courseName,
        String title,
        String content,
        boolean isPinned,
        boolean isRead,
        LocalDateTime createdAt,
        PreviousNoticeResult previousNotice
) {
    public record PreviousNoticeResult(Long noticeId, String title) {}
}