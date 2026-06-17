package com.wanted.backend.domain.report_moderation.presentation.response;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportListResult;

import java.util.List;

public record AdminReportListResponse(
        List<AdminReportItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static AdminReportListResponse from(AdminReportListResult result) {
        return new AdminReportListResponse(
                result.items().stream()
                        .map(AdminReportItemResponse::from)
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }
}
