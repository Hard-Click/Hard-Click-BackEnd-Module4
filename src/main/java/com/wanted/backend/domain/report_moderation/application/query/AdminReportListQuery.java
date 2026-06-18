package com.wanted.backend.domain.report_moderation.application.query;

import com.wanted.backend.domain.community.domain.model.ReportStatus;
import com.wanted.backend.domain.community.domain.model.TargetType;

public record AdminReportListQuery(
        ReportStatus status,
        TargetType targetType,
        int page,
        int size
) {
}
