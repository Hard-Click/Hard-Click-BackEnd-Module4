package com.wanted.backend.domain.report_moderation.application.port;

import com.wanted.backend.domain.report_moderation.application.command.AdminContentStatusCommand;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentResult;

public interface AdminContentCommandPort {

    AdminContentResult changeStatus(AdminContentStatusCommand command);
}
