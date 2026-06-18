package com.wanted.backend.domain.report_moderation.application.dto;

import com.wanted.backend.domain.community.domain.model.ReportStatus;
import com.wanted.backend.domain.community.domain.model.TargetType;

import java.time.LocalDateTime;
import java.util.List;

public record AdminReportListResult(
        List<Item> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public record Item(
            Long reportId,
            TargetType targetType,
            Long targetId,
            String targetTitle,
            String targetContentPreview,
            String reason,
            Long targetAuthorId,
            String targetAuthorName,
            int reportCount,
            ReportStatus status,
            LocalDateTime reportedAt
    ) {
    }
}
