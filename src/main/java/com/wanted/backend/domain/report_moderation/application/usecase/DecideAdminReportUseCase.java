package com.wanted.backend.domain.report_moderation.application.usecase;

import com.wanted.backend.domain.report_moderation.application.command.AdminReportDecisionCommand;
import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDecisionResult;

public interface DecideAdminReportUseCase {

    AdminReportDecisionResult decide(AdminReportDecisionCommand command);
}