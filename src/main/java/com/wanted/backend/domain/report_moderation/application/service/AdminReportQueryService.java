package com.wanted.backend.domain.report_moderation.application.service;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportListResult;
import com.wanted.backend.domain.report_moderation.application.port.AdminReportQueryPort;
import com.wanted.backend.domain.report_moderation.application.query.AdminReportListQuery;
import com.wanted.backend.domain.report_moderation.application.usecase.GetAdminReportListUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportQueryService implements GetAdminReportListUseCase {

    private final AdminReportQueryPort adminReportQueryPort;

    @Override
    public AdminReportListResult getList(AdminReportListQuery query) {
        return adminReportQueryPort.findReports(query);
    }
}
