package com.wanted.backend.domain.report_moderation.presentation.request;

import com.wanted.backend.domain.report_moderation.application.command.AdminReportDecisionCommand;
import com.wanted.backend.domain.report_moderation.domain.model.AdminReportDecision;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminReportDecisionRequest(
        @NotBlank
        String decision,

        @Size(max = 500)
        String memo,

        @NotNull
        Boolean deleteTarget
) {
    public AdminReportDecisionCommand toCommand(Long reportId) {
        return new AdminReportDecisionCommand(
                reportId,
                parseDecision(decision),
                normalizeMemo(memo),
                deleteTarget
        );
    }

    private AdminReportDecision parseDecision(String value) {
        try {
            return AdminReportDecision.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, e);
        }
    }

    private String normalizeMemo(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}