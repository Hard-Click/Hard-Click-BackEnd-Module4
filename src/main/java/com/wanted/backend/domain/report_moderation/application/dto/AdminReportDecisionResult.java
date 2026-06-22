package com.wanted.backend.domain.report_moderation.application.dto;

import com.wanted.backend.domain.community.domain.model.ReportStatus;
import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.report_moderation.domain.model.AdminReportDecision;

public record AdminReportDecisionResult(
        Long reportId,
        AdminReportDecision decision,
        ReportStatus status,
        String memo,
        TargetType targetType,
        Long targetId,
        boolean targetDeleted,
        Long targetAuthorId
) {
}