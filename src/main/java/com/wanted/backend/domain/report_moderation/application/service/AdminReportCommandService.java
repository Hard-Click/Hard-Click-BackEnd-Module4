package com.wanted.backend.domain.report_moderation.application.service;

import com.wanted.backend.domain.report_moderation.application.command.AdminReportDecisionCommand;
import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDecisionResult;
import com.wanted.backend.domain.report_moderation.application.port.AdminReportCommandPort;
import com.wanted.backend.domain.report_moderation.application.usecase.DecideAdminReportUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportCommandService implements DecideAdminReportUseCase {
    private final AdminReportCommandPort adminReportCommandPort;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "adminDashboard", key = "'summary'")
    public AdminReportDecisionResult decide(AdminReportDecisionCommand command){
        return adminReportCommandPort.decide(command);
    }
}
