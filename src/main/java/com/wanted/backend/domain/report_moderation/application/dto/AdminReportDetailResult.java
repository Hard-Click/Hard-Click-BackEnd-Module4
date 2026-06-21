package com.wanted.backend.domain.report_moderation.application.dto;

import com.wanted.backend.domain.community.domain.model.ReportStatus;
import com.wanted.backend.domain.community.domain.model.ReportType;
import com.wanted.backend.domain.community.domain.model.TargetType;

import java.util.List;

public record AdminReportDetailResult(
        Long reportId,
        TargetType targetType,
        Long targetId,
        String targetTitle,
        String targetContent,
        String targetUrl,
        Long targetAuthorId,
        String targetAuthorName,
        int reportCount,
        List<ReasonCount> reasonCounts,
        Long reporterId,
        String reporterName,
        String reporterUsername,
        ReportStatus status,
        String memo
) {
    public record ReasonCount(
            ReportType reason,
            int count
    ) {
    }
}
