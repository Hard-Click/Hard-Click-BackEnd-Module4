package com.wanted.backend.domain.report_moderation.presentation.response;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDetailResult;

import java.util.List;

public record AdminReportDetailResponse(
        Long reportId,
        String targetType,
        Long targetId,
        String targetTitle,
        String targetContent,
        String targetUrl,
        Long targetAuthorId,
        String targetAuthorName,
        int reportCount,
        List<ReasonCountResponse> reasonCounts,
        Long reporterId,
        String reporterName,
        String reporterUsername,
        String status,
        String memo
) {
    public static AdminReportDetailResponse from(AdminReportDetailResult result) {
        return new AdminReportDetailResponse(
                result.reportId(),
                result.targetType().name(),
                result.targetId(),
                result.targetTitle(),
                result.targetContent(),
                result.targetUrl(),
                result.targetAuthorId(),
                result.targetAuthorName(),
                result.reportCount(),
                result.reasonCounts().stream()
                        .map(ReasonCountResponse::from)
                        .toList(),
                result.reporterId(),
                result.reporterName(),
                result.reporterUsername(),
                result.status().name(),
                result.memo()
        );
    }

    public record ReasonCountResponse(
            String reason,
            int count
    ) {
        private static ReasonCountResponse from(AdminReportDetailResult.ReasonCount reasonCount) {
            return new ReasonCountResponse(reasonCount.reason().name(), reasonCount.count());
        }
    }
}
