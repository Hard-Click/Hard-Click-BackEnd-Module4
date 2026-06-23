package com.wanted.backend.domain.report_moderation.presentation.request;

import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.report_moderation.application.command.ChangeAdminContentStatusCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record AdminContentStatusRequest(
        @Schema(description = "변경할 콘텐츠 상태", example = "ADMIN_DELETED")
        @NotBlank
        String status,

        @Schema(description = "처리 메모", example = "스팸 광고로 신고 누적되어 삭제 처리")
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
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeMemo(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
