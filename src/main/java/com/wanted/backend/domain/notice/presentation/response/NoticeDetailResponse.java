package com.wanted.backend.domain.notice.presentation.response;

import java.time.LocalDateTime;

public record NoticeDetailResponse(
        Long noticeId,
        String noticeType,
        String courseName,
        String title,
        String content,
        boolean isPinned,
        boolean isRead,         // 추후 구현 예정
        LocalDateTime createdAt,
        PreviousNotice previousNotice
) {
    public record PreviousNotice(
            Long noticeId,
            String title
    ) {

    }
}