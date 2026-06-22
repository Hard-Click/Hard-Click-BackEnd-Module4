package com.wanted.backend.domain.report_moderation.application.port;

import com.wanted.backend.domain.report_moderation.application.command.AdminReportDecisionCommand;
import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDecisionResult;

public interface AdminReportCommandPort {

    AdminReportDecisionResult decide(AdminReportDecisionCommand command);

}
