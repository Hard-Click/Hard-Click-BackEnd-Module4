package com.wanted.backend.domain.report_moderation.application.service;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDetailResult;
import com.wanted.backend.domain.report_moderation.application.dto.AdminReportListResult;
import com.wanted.backend.domain.report_moderation.application.port.AdminReportQueryPort;
import com.wanted.backend.domain.report_moderation.application.query.AdminReportListQuery;
import com.wanted.backend.domain.report_moderation.application.usecase.GetAdminReportDetailUseCase;
import com.wanted.backend.domain.report_moderation.application.usecase.GetAdminReportListUseCase;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportQueryService implements GetAdminReportListUseCase, GetAdminReportDetailUseCase {

    private final AdminReportQueryPort adminReportQueryPort;

    @Override
    public AdminReportListResult getList(AdminReportListQuery query) {
        return adminReportQueryPort.findReports(query);
    }

    @Override
    public AdminReportDetailResult getDetail(Long reportId) {
        return adminReportQueryPort.findReportDetail(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
    }
}
