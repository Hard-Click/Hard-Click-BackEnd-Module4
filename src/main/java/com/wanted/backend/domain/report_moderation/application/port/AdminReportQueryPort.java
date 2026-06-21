package com.wanted.backend.domain.report_moderation.application.port;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDetailResult;
import com.wanted.backend.domain.report_moderation.application.dto.AdminReportListResult;
import com.wanted.backend.domain.report_moderation.application.query.AdminReportListQuery;

import java.util.Optional;

public interface AdminReportQueryPort {
    AdminReportListResult findReports(AdminReportListQuery query);
    Optional<AdminReportDetailResult> findReportDetail(Long reportId);
}
