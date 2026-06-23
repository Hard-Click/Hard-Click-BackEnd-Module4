package com.wanted.backend.domain.report_moderation.presentation.response;

import com.wanted.backend.domain.report_moderation.application.dto.AdminContentResult;

public record AdminContentResponse(
        String contentType,
        Long contentId,
        String title,
        String content,
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
