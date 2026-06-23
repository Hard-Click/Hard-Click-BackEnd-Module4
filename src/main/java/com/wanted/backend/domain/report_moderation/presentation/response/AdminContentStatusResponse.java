package com.wanted.backend.domain.report_moderation.presentation.response;

import com.wanted.backend.domain.report_moderation.application.dto.AdminContentStatusResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminContentStatusResponse(
        @Schema(description = "콘텐츠 타입", example = "POST")
        String contentType,

        @Schema(description = "콘텐츠 ID", example = "15")
        Long contentId,

        @Schema(description = "변경된 콘텐츠 상태", example = "ADMIN_DELETED")
        String status
) {
    public static AdminContentStatusResponse from(AdminContentStatusResult result) {
        return new AdminContentStatusResponse(
                result.contentType().name(),
                result.contentId(),
                result.status()
        );
    }
}
