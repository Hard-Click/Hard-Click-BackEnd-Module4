package com.wanted.backend.domain.admin_dashboard.presentation.response;

import com.wanted.backend.domain.admin_dashboard.application.dto.AdminDashboardResult;

import java.time.LocalDateTime;
import java.util.List;

public record AdminDashboardResponse(
        long totalMemberCount,
        long pendingReportCount,
        long totalCourseCount,
        long totalNoticeCount,
        List<RecentReportResponse> recentReports,
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

    public record RecentReportResponse(
            Long reportId,
            String targetType,
            String targetTitle,
            String reason,
            String status,
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

    public record RecentNoticeResponse(
            Long noticeId,
            String title,
            boolean isImportant,
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