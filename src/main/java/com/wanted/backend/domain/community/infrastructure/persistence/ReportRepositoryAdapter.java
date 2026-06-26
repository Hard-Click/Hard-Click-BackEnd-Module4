package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.Report;
import com.wanted.backend.domain.community.domain.model.ReportType;
import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.community.domain.repository.ReportRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ReportRepositoryAdapter implements ReportRepository {

    private final SpringDataReportRepository repository;

    public ReportRepositoryAdapter(SpringDataReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public Long save(Report report) {
        ReportJpaEntity entity = new ReportJpaEntity(
                report.getReporterId(),
                report.getReportedMemberId(),
                report.getTargetType(),
                report.getTargetId(),
                toCommaSeparated(report.getReportTypes()),
                report.getReason(),
                report.getCreatedAt()
        );
        return repository.save(entity).getId();
    }

    @Override
    public boolean existsByReporterIdAndTargetTypeAndTargetId(
            Long reporterId, TargetType targetType, Long targetId) {
        return repository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, targetType, targetId);
    }

    @Override
    public int countByTargetTypeAndTargetId(TargetType targetType, Long targetId) {
        return repository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    private String toCommaSeparated(List<ReportType> types) {
        return types.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private List<ReportType> toReportTypeList(String reportTypes) {
        return Arrays.stream(reportTypes.split(","))
                .map(ReportType::valueOf)
                .toList();
    }
    @Override
    public int countByReportedMemberId(Long reportedMemberId) {
        return repository.countByReportedMemberId(reportedMemberId);
    }
}
