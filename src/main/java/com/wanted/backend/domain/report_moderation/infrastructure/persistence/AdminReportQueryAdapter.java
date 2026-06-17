package com.wanted.backend.domain.report_moderation.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.ReportStatus;
import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.community.infrastructure.persistence.CommentJpaEntity;
import com.wanted.backend.domain.community.infrastructure.persistence.PostJpaEntity;
import com.wanted.backend.domain.community.infrastructure.persistence.ReportJpaEntity;
import com.wanted.backend.domain.community.infrastructure.persistence.ReviewJpaEntity;
import com.wanted.backend.domain.report_moderation.application.dto.AdminReportListResult;
import com.wanted.backend.domain.report_moderation.application.port.AdminReportQueryPort;
import com.wanted.backend.domain.report_moderation.application.query.AdminReportListQuery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminReportQueryAdapter implements AdminReportQueryPort {

    private static final int MIN_REPORT_COUNT_FOR_ADMIN_LIST = 3;
    private static final int PREVIEW_LENGTH = 50;

    private final EntityManager entityManager;

    @Override
    public AdminReportListResult findReports(AdminReportListQuery query) {
        List<ReportTargetGroup> groups = findTargetGroups(query.targetType()).stream()
                .map(this::toReportTargetGroup)
                .filter(group -> query.status() == null || group.status() == query.status())
                .sorted(Comparator.comparing(ReportTargetGroup::reportedAt)
                        .thenComparing(ReportTargetGroup::reportId)
                        .reversed())
                .toList();

        int totalElements = groups.size();
        int totalPages = calculateTotalPages(totalElements, query.size());
        int fromIndex = Math.min(query.page() * query.size(), totalElements);
        int toIndex = Math.min(fromIndex + query.size(), totalElements);
        List<ReportTargetGroup> pageGroups = groups.subList(fromIndex, toIndex);

        return new AdminReportListResult(
                pageGroups.stream()
                        .map(this::toItem)
                        .toList(),
                query.page(),
                query.size(),
                totalElements,
                totalPages,
                toIndex < totalElements
        );
    }

    private List<Object[]> findTargetGroups(TargetType targetType) {
        return entityManager.createQuery("""
                select r.targetType, r.targetId, count(r.id)
                from ReportJpaEntity r
                where (:targetType is null or r.targetType = :targetType)
                group by r.targetType, r.targetId
                having count(r.id) >= :minReportCount
                """, Object[].class)
                .setParameter("targetType", targetType)
                .setParameter("minReportCount", (long) MIN_REPORT_COUNT_FOR_ADMIN_LIST)
                .getResultList();
    }

    private ReportTargetGroup toReportTargetGroup(Object[] row) {
        TargetType targetType = (TargetType) row[0];
        Long targetId = (Long) row[1];
        Long reportCount = (Long) row[2];
        ReportJpaEntity latestReport = findLatestReport(targetType, targetId);

        return new ReportTargetGroup(
                latestReport.getId(),
                targetType,
                targetId,
                latestReport.getReportedMemberId(),
                reportCount.intValue(),
                normalizeStatus(latestReport.getStatus()),
                latestReport.getReportTypes(),
                latestReport.getCreatedAt()
        );
    }

    private ReportJpaEntity findLatestReport(TargetType targetType, Long targetId) {
        return entityManager.createQuery("""
                select r
                from ReportJpaEntity r
                where r.targetType = :targetType
                  and r.targetId = :targetId
                order by r.createdAt desc, r.id desc
                """, ReportJpaEntity.class)
                .setParameter("targetType", targetType)
                .setParameter("targetId", targetId)
                .setMaxResults(1)
                .getSingleResult();
    }

    private AdminReportListResult.Item toItem(ReportTargetGroup group) {
        TargetContent targetContent = findTargetContent(group.targetType(), group.targetId());

        return new AdminReportListResult.Item(
                group.reportId(),
                group.targetType(),
                group.targetId(),
                targetContent.title(),
                preview(targetContent.content()),
                representativeReason(group.reportTypes()),
                group.targetAuthorId(),
                findMemberName(group.targetAuthorId()),
                group.reportCount(),
                group.status(),
                group.reportedAt()
        );
    }

    private TargetContent findTargetContent(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case POST -> {
                PostJpaEntity post = entityManager.find(PostJpaEntity.class, targetId);
                yield post == null
                        ? new TargetContent("삭제된 게시글", "삭제된 게시글입니다.")
                        : new TargetContent(post.getTitle(), post.getContent());
            }
            case COMMENT -> {
                CommentJpaEntity comment = entityManager.find(CommentJpaEntity.class, targetId);
                yield comment == null
                        ? new TargetContent("댓글 내용", "삭제된 댓글입니다.")
                        : new TargetContent("댓글 내용", comment.getContent());
            }
            case REVIEW -> {
                ReviewJpaEntity review = entityManager.find(ReviewJpaEntity.class, targetId);
                yield review == null
                        ? new TargetContent("리뷰 내용", "삭제된 리뷰입니다.")
                        : new TargetContent("리뷰 내용", review.getContent());
            }
        };
    }

    private String findMemberName(Long memberId) {
        if (memberId == null) {
            return null;
        }

        List<?> names = entityManager.createNativeQuery(
                        "select name from members where member_id = :memberId")
                .setParameter("memberId", memberId)
                .setMaxResults(1)
                .getResultList();

        return names.isEmpty() ? null : String.valueOf(names.get(0));
    }

    private ReportStatus normalizeStatus(ReportStatus status) {
        return status == null ? ReportStatus.PENDING : status;
    }

    private String representativeReason(String reportTypes) {
        if (reportTypes == null || reportTypes.isBlank()) {
            return null;
        }
        return reportTypes.split(",")[0].trim();
    }

    private String preview(String content) {
        if (content == null || content.length() <= PREVIEW_LENGTH) {
            return content;
        }
        return content.substring(0, PREVIEW_LENGTH) + "...";
    }

    private int calculateTotalPages(int totalElements, int size) {
        if (totalElements == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / size);
    }

    private record ReportTargetGroup(
            Long reportId,
            TargetType targetType,
            Long targetId,
            Long targetAuthorId,
            int reportCount,
            ReportStatus status,
            String reportTypes,
            LocalDateTime reportedAt
    ) {
    }

    private record TargetContent(
            String title,
            String content
    ) {
    }
}
