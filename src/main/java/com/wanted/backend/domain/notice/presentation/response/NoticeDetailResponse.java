package com.wanted.backend.domain.notice.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wanted.backend.domain.notice.application.result.NoticeDetailResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NoticeDetailResponse(

        @Schema(description = "공지사항 ID", example = "5")
        Long noticeId,

        @Schema(description = "공지사항 타입 (GLOBAL: 전체 공지, COURSE: 강의 공지)", example = "COURSE")
        String noticeType,

        @Schema(description = "강의명 (COURSE 타입일 경우)", example = "Spring Boot 완전 정복")
        String courseName,

        @Schema(description = "공지사항 제목", example = "3주차 강의 업로드 안내")
        String title,

        @Schema(description = "공지사항 내용", example = "3주차 강의가 업로드되었습니다.")
        String content,

        @Schema(description = "상단 고정 여부", example = "false")
        boolean isPinned,

        @Schema(description = "읽음 여부", example = "false")
        boolean isRead,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss+09:00")
        @Schema(description = "작성일시", example = "2025-03-15T14:30:00")
        LocalDateTime createdAt,

        @Schema(description = "이전 공지사항 정보")
        PreviousNotice previousNotice
) {
    public record PreviousNotice(
            @Schema(description = "이전 공지사항 ID", example = "4")
            Long noticeId,
            @Schema(description = "이전 공지사항 제목", example = "2주차 강의 업로드 안내")
            String title
    ) {}

    public static NoticeDetailResponse from(NoticeDetailResult result) {
        PreviousNotice prev = result.previousNotice() != null
                ? new PreviousNotice(result.previousNotice().noticeId(), result.previousNotice().title())
                : null;
        return new NoticeDetailResponse(
                result.noticeId(), result.noticeType(), result.courseName(),
                result.title(), result.content(), result.isPinned(),
                result.isRead(), result.createdAt(), prev);
    }
}