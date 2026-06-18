package com.wanted.backend.domain.report_moderation.application.port;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportListResult;
import com.wanted.backend.domain.report_moderation.application.query.AdminReportListQuery;

public interface AdminReportQueryPort {
    AdminReportListResult findReports(AdminReportListQuery query);
}
