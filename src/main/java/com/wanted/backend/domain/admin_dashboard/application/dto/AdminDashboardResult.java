package com.wanted.backend.domain.admin_dashboard.application.dto;

import com.wanted.backend.domain.community.domain.model.ReportStatus;
import com.wanted.backend.domain.community.domain.model.ReportType;
import com.wanted.backend.domain.community.domain.model.TargetType;

import java.time.LocalDateTime;
import java.util.List;

public record AdminDashboardResult(
        long totalMemberCount,
        long pendingReportCount,
        long totalCourseCount,
        long totalNoticeCount,
        List<RecentReport> recentReports,
        List<RecentNotice> recentNotices
) {
    public record RecentReport(
            Long reportId,
            TargetType targetType,
            String targetTitle,
            ReportType reason,
            ReportStatus status,
            LocalDateTime reportedAt
    ) {
    }

    public record RecentNotice(
            Long noticeId,
            String title,
            boolean isImportant,
            LocalDateTime createdAt
    ) {
    }
}