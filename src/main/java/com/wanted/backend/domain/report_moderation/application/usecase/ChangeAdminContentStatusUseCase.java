package com.wanted.backend.domain.report_moderation.application.usecase;

import com.wanted.backend.domain.report_moderation.application.command.AdminContentStatusCommand;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentResult;

public interface ChangeAdminContentStatusUseCase {

    AdminContentResult changeStatus(AdminContentStatusCommand command);
}
