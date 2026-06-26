package com.wanted.backend.domain.report_moderation.presentation.response;

import com.wanted.backend.domain.report_moderation.application.dto.AdminContentResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminContentResponse(
        @Schema(description = "콘텐츠 타입", example = "POST")
        String contentType,

        @Schema(description = "콘텐츠 ID", example = "15")
        Long contentId,

        @Schema(description = "제목 (댓글·리뷰는 null)", example = "JWT 필터 질문입니다")
        String title,

        @Schema(description = "콘텐츠 내용", example = "OncePerRequestFilter를 상속받아 구현합니다.")
        String content,

        @Schema(description = "콘텐츠 상태", example = "ACTIVE")
        String status
) {
    public static AdminContentResponse from(AdminContentResult result) {
        return new AdminContentResponse(
                result.contentType().name(),
                result.contentId(),
                result.title(),
                result.content(),
                result.status()
        );
    }
}
