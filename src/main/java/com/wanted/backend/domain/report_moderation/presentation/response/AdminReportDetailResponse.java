package com.wanted.backend.domain.report_moderation.presentation.response;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDetailResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AdminReportDetailResponse(
        @Schema(description = "신고 ID", example = "101")
        Long reportId,

        @Schema(description = "신고 대상 타입", example = "POST")
        String targetType,

        @Schema(description = "신고 대상 ID", example = "15")
        Long targetId,

        @Schema(description = "신고 대상 제목 (댓글·리뷰는 null)", example = "React Hook useEffect 사용 시 무한 루프")
        String targetTitle,

        @Schema(description = "신고 대상 내용", example = "useEffect를 사용할 때 계속 무한 루프가 발생합니다...")
        String targetContent,

        @Schema(description = "신고 대상 콘텐츠 원본 URL", example = "/posts/15")
        String targetUrl,

        @Schema(description = "신고 대상 작성자 ID", example = "42")
        Long targetAuthorId,

        @Schema(description = "신고 대상 작성자 이름", example = "최수진")
        String targetAuthorName,

        @Schema(description = "누적 신고 횟수", example = "7")
        int reportCount,

        @Schema(description = "신고 사유별 횟수")
        List<ReasonCountResponse> reasonCounts,

        @Schema(description = "신고자 ID", example = "10")
        Long reporterId,

        @Schema(description = "신고자 이름", example = "박지영")
        String reporterName,

        @Schema(description = "신고자 아이디", example = "jiyoung_park")
        String reporterUsername,

        @Schema(description = "처리 상태", example = "PENDING")
        String status,

        @Schema(description = "처리 메모", example = "스팸 광고로 신고 누적되어 삭제 처리")
        String memo
) {
    public static AdminReportDetailResponse from(AdminReportDetailResult result) {
        return new AdminReportDetailResponse(
                result.reportId(),
                result.targetType().name(),
                result.targetId(),
                result.targetTitle(),
                result.targetContent(),
                result.targetUrl(),
                result.targetAuthorId(),
                result.targetAuthorName(),
                result.reportCount(),
                result.reasonCounts().stream()
                        .map(ReasonCountResponse::from)
                        .toList(),
                result.reporterId(),
                result.reporterName(),
                result.reporterUsername(),
                result.status().name(),
                result.memo()
        );
    }

    public record ReasonCountResponse(
            String reason,
            int count
    ) {
        private static ReasonCountResponse from(AdminReportDetailResult.ReasonCount reasonCount) {
            return new ReasonCountResponse(reasonCount.reason().name(), reasonCount.count());
        }
    }
}
