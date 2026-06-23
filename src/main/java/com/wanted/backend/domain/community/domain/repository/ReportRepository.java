package com.wanted.backend.domain.community.domain.repository;

import com.wanted.backend.domain.community.domain.model.Report;
import com.wanted.backend.domain.community.domain.model.TargetType;

public interface ReportRepository {
    Long save(Report report);
    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, TargetType targetType, Long targetId);
    int countByTargetTypeAndTargetId(TargetType targetType, Long targetId);
    int countByReportedMemberId(Long reportedMemberId);
}