package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReportRepository extends JpaRepository<ReportJpaEntity, Long> {
    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, TargetType targetType, Long targetId);
    int countByTargetTypeAndTargetId(TargetType targetType, Long targetId);

}