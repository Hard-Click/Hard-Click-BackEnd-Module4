package com.wanted.backend.domain.report_moderation.presentation.response;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDecisionResult;

public record AdminReportDecisionResponse(
        Long reportId,
        String decision,
        String status,
        String memo,
        String targetType,
        Long targetId,
        boolean targetDeleted,
        Long targetAuthorId
) {
    public static AdminReportDecisionResponse from(AdminReportDecisionResult result) {
        return new AdminReportDecisionResponse(
                result.reportId(),
                result.decision().name(),
                result.status().name(),
                result.memo(),
                result.targetType().name(),
                result.targetId(),
                result.targetDeleted(),
                result.targetAuthorId()
        );
    }
}