package com.wanted.backend.domain.notice.presentation.response;

import java.time.LocalDateTime;


public record NoticeItemResponse(
        Long noticeId,
        String noticeType,  // GLOBAL / COURSE
        String courseName,
        String title,
        boolean isPinned,
        boolean isRead,       // 추후 구현 예정
        LocalDateTime createdAt
) {

}