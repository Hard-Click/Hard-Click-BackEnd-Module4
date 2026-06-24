package com.wanted.backend.domain.report_moderation.presentation.response;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDecisionResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminReportDecisionResponse(
        @Schema(description = "신고 ID", example = "101")
        Long reportId,

        @Schema(description = "처리 결정", example = "DELETE")
        String decision,

        @Schema(description = "처리 후 신고 상태", example = "RESOLVED")
        String status,

        @Schema(description = "처리 메모", example = "스팸 광고로 신고 누적되어 삭제 처리")
        String memo,

        @Schema(description = "신고 대상 타입", example = "POST")
        String targetType,

        @Schema(description = "신고 대상 ID", example = "15")
        Long targetId,

        @Schema(description = "신고 대상 콘텐츠 삭제 여부", example = "true")
        boolean targetDeleted,

        @Schema(description = "신고 대상 작성자 ID", example = "42")
        Long targetAuthorId
) {
    public static AdminReportDecisionResponse from(AdminReportDecisionResult result) {
        return new AdminReportDecisionResponse(
                result.reportId(),
                result.decision().name(),
                result.status().name(),
                result.memo(),
                result.targetType().name(),
                result.targetId(),
                result.targetDeleted(),
                result.targetAuthorId()
        );
    }
}