package com.wanted.backend.domain.report_moderation.presentation.response;

import com.wanted.backend.domain.report_moderation.application.dto.AdminContentStatusResult;

public record AdminContentStatusResponse(
        String contentType,
        Long contentId,
        String status,
        String memo
) {
    public static AdminContentStatusResponse from(AdminContentStatusResult result) {
        return new AdminContentStatusResponse(
                result.contentType().name(),
                result.contentId(),
                result.status(),
                result.memo()
        );
    }
}
