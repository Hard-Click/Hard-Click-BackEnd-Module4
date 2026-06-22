package com.wanted.backend.domain.report_moderation.presentation.request;

import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.report_moderation.application.command.ChangeAdminContentStatusCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminContentStatusRequest(
        @NotBlank
        String status,

        @Size(max = 500)
        String memo
) {
    public ChangeAdminContentStatusCommand toCommand(TargetType contentType, Long contentId) {
        return new ChangeAdminContentStatusCommand(
                contentType,
                contentId,
                normalizeStatus(status),
                normalizeMemo(memo)
        );
    }

    private String normalizeStatus(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeMemo(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
