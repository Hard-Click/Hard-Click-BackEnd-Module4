package com.wanted.backend.domain.report_moderation.application.command;

import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.report_moderation.domain.model.AdminContentStatus;

public record AdminContentStatusCommand(
        TargetType contentType,
        Long contentId,
        AdminContentStatus status
) {
}
