package com.wanted.backend.domain.report_moderation.application.command;

import com.wanted.backend.domain.report_moderation.domain.model.AdminReportDecision;

public record AdminReportDecisionCommand(
        Long reportId,
        AdminReportDecision decision,
        String memo,
        boolean deleteTarget
) {
}