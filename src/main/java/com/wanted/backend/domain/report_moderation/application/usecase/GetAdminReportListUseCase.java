package com.wanted.backend.domain.report_moderation.application.usecase;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportListResult;
import com.wanted.backend.domain.report_moderation.application.query.AdminReportListQuery;

public interface GetAdminReportListUseCase {
    AdminReportListResult getList(AdminReportListQuery query);
}
