package com.wanted.backend.domain.report_moderation.application.usecase;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDetailResult;

public interface GetAdminReportDetailUseCase {
    AdminReportDetailResult getDetail(Long reportId);
}
