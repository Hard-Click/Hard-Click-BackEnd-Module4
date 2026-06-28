package com.wanted.backend.domain.notice.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wanted.backend.domain.notice.application.result.NoticeItemResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NoticeItemResponse(

        @Schema(description = "공지사항 ID", example = "5")
        Long noticeId,

        @Schema(description = "공지사항 타입 (GLOBAL: 전체 공지, COURSE: 강의 공지)", example = "COURSE")
        String noticeType,

        @Schema(description = "강의명 (COURSE 타입일 경우)", example = "Spring Boot 완전 정복")
        String courseName,

        @Schema(description = "공지사항 제목", example = "3주차 강의 업로드 안내")
        String title,

        @Schema(description = "상단 고정 여부", example = "false")
        boolean isPinned,

        @Schema(description = "읽음 여부", example = "false")
        boolean isRead,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss+09:00")
        @Schema(description = "작성일시", example = "2025-03-15T14:30:00")
        LocalDateTime createdAt
) {
        public static NoticeItemResponse from(NoticeItemResult result) {
                return new NoticeItemResponse(
                        result.noticeId(), result.noticeType(), result.courseName(),
                        result.title(), result.isPinned(), result.isRead(), result.createdAt());
        }
}