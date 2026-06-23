package com.wanted.backend.domain.report_moderation.application.command;

import com.wanted.backend.domain.community.domain.model.TargetType;

public record ChangeAdminContentStatusCommand(
        TargetType contentType,
        Long contentId,
        String status
) {
}
