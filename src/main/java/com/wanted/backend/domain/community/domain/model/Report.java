package com.wanted.backend.domain.community.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class Report {

    private Long id;
    private Long reporterId;
    private Long reportedMemberId;
    private TargetType targetType;
    private Long targetId;
    private List<ReportType> reportTypes;
    private String reason;
    private LocalDateTime createdAt;

    private Report(Long id, Long reporterId, Long reportedMemberId, TargetType targetType,
                   Long targetId, List<ReportType> reportTypes,
                   String reason, LocalDateTime createdAt) {
        this.id = id;
        this.reporterId = reporterId;
        this.reportedMemberId = reportedMemberId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reportTypes = reportTypes;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static Report create(Long reporterId, Long reportedMemberId, TargetType targetType,
                                Long targetId, List<ReportType> reportTypes,
                                String reason) {
        return new Report(null, reporterId, reportedMemberId, targetType, targetId,
                reportTypes, reason, LocalDateTime.now());
    }

    public static Report restore(Long id, Long reporterId, Long reportedMemberId, TargetType targetType,
                                 Long targetId, List<ReportType> reportTypes,
                                 String reason, LocalDateTime createdAt) {
        return new Report(id, reporterId, reportedMemberId, targetType, targetId,
                reportTypes, reason, createdAt);
    }

    public Long getId() { return id; }
    public Long getReporterId() { return reporterId; }
    public Long getReportedMemberId() { return reportedMemberId; }
    public TargetType getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public List<ReportType> getReportTypes() { return reportTypes; }
    public String getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
