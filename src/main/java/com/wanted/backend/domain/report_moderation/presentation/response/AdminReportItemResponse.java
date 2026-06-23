package com.wanted.backend.domain.report_moderation.presentation.response;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportListResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminReportItemResponse(
        @Schema(description = "신고 ID", example = "101")
        Long reportId,

        @Schema(description = "신고 대상 타입", example = "POST")
        String targetType,

        @Schema(description = "신고 대상 ID", example = "15")
        Long targetId,

        @Schema(description = "신고 대상 제목 (댓글·리뷰는 null)", example = "React Hook useEffect 사용 시 무한 루프")
        String targetTitle,

        @Schema(description = "신고 대상 내용 미리보기", example = "useEffect를 사용할 때 계속 무한 루프가 발생합니다...")
        String targetContentPreview,

        @Schema(description = "신고 사유", example = "SPAM")
        String reason,

        @Schema(description = "신고 대상 작성자 ID", example = "42")
        Long targetAuthorId,

        @Schema(description = "신고 대상 작성자 이름", example = "이준호")
        String targetAuthorName,

        @Schema(description = "누적 신고 횟수", example = "3")
        int reportCount,

        @Schema(description = "처리 상태", example = "PENDING")
        String status,

        @Schema(description = "신고일시", example = "2026-05-11T14:30:00")
        LocalDateTime reportedAt
) {
    public static AdminReportItemResponse from(AdminReportListResult.Item item) {
        return new AdminReportItemResponse(
                item.reportId(),
                item.targetType().name(),
                item.targetId(),
                item.targetTitle(),
                item.targetContentPreview(),
                item.reason(),
                item.targetAuthorId(),
                item.targetAuthorName(),
                item.reportCount(),
                item.status().name(),
                item.reportedAt()
        );
    }
}
