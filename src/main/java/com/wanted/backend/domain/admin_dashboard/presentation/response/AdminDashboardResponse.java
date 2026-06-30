package com.wanted.backend.domain.admin_dashboard.presentation.response;

import com.wanted.backend.domain.admin_dashboard.application.dto.AdminDashboardResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "관리자 대시보드 응답")
public record AdminDashboardResponse(
        @Schema(description = "전체 회원 수", example = "3842")
        long totalMemberCount,

        @Schema(description = "처리 대기 중인 신고 수", example = "12")
        long pendingReportCount,

        @Schema(description = "전체 강의 수", example = "248")
        long totalCourseCount,

        @Schema(description = "전체 공지 수", example = "37")
        long totalNoticeCount,

        @Schema(description = "최근 신고 목록")
        List<RecentReportResponse> recentReports,

        @Schema(description = "최근 공지 목록")
        List<RecentNoticeResponse> recentNotices
) {
    public static AdminDashboardResponse from(AdminDashboardResult result) {
        return new AdminDashboardResponse(
                result.totalMemberCount(),
                result.pendingReportCount(),
                result.totalCourseCount(),
                result.totalNoticeCount(),
                result.recentReports().stream()
                        .map(RecentReportResponse::from)
                        .toList(),
                result.recentNotices().stream()
                        .map(RecentNoticeResponse::from)
                        .toList()
        );
    }

    @Schema(description = "최근 신고 항목")
    public record RecentReportResponse(
            @Schema(description = "신고 ID", example = "101")
            Long reportId,

            @Schema(description = "신고 대상 타입", example = "POST")
            String targetType,

            @Schema(description = "신고 대상 제목 (댓글·리뷰는 null)", example = "React Hook useEffect 무한 루프")
            String targetTitle,

            @Schema(description = "신고 사유 (null인 경우 있음)", example = "SPAM")
            String reason,

            @Schema(description = "신고 처리 상태", example = "PENDING")
            String status,

            @Schema(description = "신고 일시", example = "2026-05-11T14:30:00")
            LocalDateTime reportedAt
    ) {
        private static RecentReportResponse from(
                AdminDashboardResult.RecentReport report
        ) {
            return new RecentReportResponse(
                    report.reportId(),
                    report.targetType().name(),
                    report.targetTitle(),
                    report.reason() == null
                            ? null
                            : report.reason().name(),
                    report.status().name(),
                    report.reportedAt()
            );
        }
    }

    @Schema(description = "최근 공지 항목")
    public record RecentNoticeResponse(
            @Schema(description = "공지 ID", example = "5")
            Long noticeId,

            @Schema(description = "공지 제목", example = "6월 서비스 점검 안내")
            String title,

            @Schema(description = "중요 공지 여부", example = "true")
            boolean isImportant,

            @Schema(description = "공지 생성 일시", example = "2026-06-01T09:00:00")
            LocalDateTime createdAt
    ) {
        private static RecentNoticeResponse from(
                AdminDashboardResult.RecentNotice notice
        ) {
            return new RecentNoticeResponse(
                    notice.noticeId(),
                    notice.title(),
                    notice.isImportant(),
                    notice.createdAt()
            );
        }
    }
}