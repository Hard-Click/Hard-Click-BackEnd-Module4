package com.wanted.backend.domain.report_moderation.presentation.response;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportListResult;

import java.time.LocalDateTime;

public record AdminReportItemResponse(
        Long reportId,
        String targetType,
        Long targetId,
        String targetTitle,
        String targetContentPreview,
        String reason,
        Long targetAuthorId,
        String targetAuthorName,
        int reportCount,
        String status,
        LocalDateTime reportedAt
) {
    public static AdminReportItemResponse from(AdminReportListResult.Item item) {
        return new AdminReportItemResponse(
                item.reportId(),
                item.targetType().name(),
                item.targetId(),
                item.targetTitle(),
                item.targetContentPreview(),
                item.reason(),
                item.targetAuthorId(),
                item.targetAuthorName(),
                item.reportCount(),
                item.status().name(),
                item.reportedAt()
        );
    }
}
