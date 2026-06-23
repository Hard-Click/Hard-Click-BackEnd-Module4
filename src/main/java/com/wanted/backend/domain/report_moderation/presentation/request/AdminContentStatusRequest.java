package com.wanted.backend.domain.report_moderation.presentation.request;

import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.report_moderation.application.command.ChangeAdminContentStatusCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Locale;

public record AdminContentStatusRequest(
        @Schema(description = "변경할 콘텐츠 상태", example = "ADMIN_DELETED")
        @NotBlank
        @Pattern(
                regexp = "ACTIVE|ADMIN_DELETED",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "status는 ACTIVE 또는 ADMIN_DELETED만 허용됩니다."
        )
        String status
) {
    public ChangeAdminContentStatusCommand toCommand(TargetType contentType, Long contentId) {
        return new ChangeAdminContentStatusCommand(
                contentType,
                contentId,
                normalizeStatus(status)
        );
    }

    private String normalizeStatus(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
