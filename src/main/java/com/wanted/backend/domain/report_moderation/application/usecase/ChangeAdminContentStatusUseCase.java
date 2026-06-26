package com.wanted.backend.domain.report_moderation.application.usecase;

import com.wanted.backend.domain.report_moderation.application.command.ChangeAdminContentStatusCommand;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentStatusResult;

public interface ChangeAdminContentStatusUseCase {
    AdminContentStatusResult changeStatus(ChangeAdminContentStatusCommand command);
}
