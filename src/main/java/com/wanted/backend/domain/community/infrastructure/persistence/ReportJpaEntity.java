package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.community.domain.model.ReportStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reports",
        indexes = {
                @Index(name = "idx_reports_reported_member_id", columnList = "reported_member_id"),
                @Index(
                        name = "idx_reports_target_latest",
                        columnList = "target_type, target_id, created_at, report_id"
                )
        }
)
@Getter
public class ReportJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "reported_member_id", nullable = false)
    private Long reportedMemberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "report_types", nullable = false)
    private String reportTypes;

    @Column(name = "reason")
    private String reason;

    @Column(name = "memo", length = 500)
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ReportJpaEntity() {}

    public ReportJpaEntity(Long reporterId, Long reportedMemberId, TargetType targetType, Long targetId,
                           String reportTypes, String reason, LocalDateTime createdAt) {
        this.reporterId = reporterId;
        this.reportedMemberId = reportedMemberId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reportTypes = reportTypes;
        this.reason = reason;
        this.status = ReportStatus.PENDING;
        this.createdAt = createdAt;
    }
    public boolean isProcessed() {
        return this.status == ReportStatus.REJECTED
                || this.status == ReportStatus.RESOLVED;
    }

    public void reject(String memo) {
        this.status = ReportStatus.REJECTED;
        this.memo = memo;
    }

    public void resolve(String memo) {
        this.status = ReportStatus.RESOLVED;
        this.memo = memo;
    }
}
