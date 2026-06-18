package com.wanted.backend.domain.report_moderation.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.Comment;
import com.wanted.backend.domain.community.domain.model.CommentStatus;
import com.wanted.backend.domain.community.domain.model.PostStatus;
import com.wanted.backend.domain.community.domain.model.ReportStatus;
import com.wanted.backend.domain.community.domain.model.ReportType;
import com.wanted.backend.domain.community.domain.model.ReviewStatus;
import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.community.infrastructure.persistence.CommentJpaEntity;
import com.wanted.backend.domain.community.infrastructure.persistence.PostJpaEntity;
import com.wanted.backend.domain.community.infrastructure.persistence.ReportJpaEntity;
import com.wanted.backend.domain.community.infrastructure.persistence.ReviewJpaEntity;
import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDetailResult;
import com.wanted.backend.domain.report_moderation.application.dto.AdminReportListResult;
import com.wanted.backend.domain.report_moderation.application.port.AdminReportQueryPort;
import com.wanted.backend.domain.report_moderation.application.query.AdminReportListQuery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminReportQueryAdapter implements AdminReportQueryPort {

    private static final int MIN_REPORT_COUNT_FOR_ADMIN_LIST = 3;
    private static final int PREVIEW_LENGTH = 50;
    private static final String DELETED_POST_TITLE = "삭제된 게시글";
    private static final String DELETED_POST_CONTENT = "삭제된 게시글입니다.";
    private static final String COMMENT_TITLE = "댓글 내용";
    private static final String DELETED_COMMENT_CONTENT = "삭제된 댓글입니다.";
    private static final String REVIEW_TITLE = "리뷰 내용";
    private static final String DELETED_REVIEW_CONTENT = "삭제된 리뷰입니다.";

    private static final String DETAIL_DELETED_POST_TITLE = "삭제된 게시글";
    private static final String DETAIL_DELETED_POST_CONTENT = "관리자에 의해 삭제된 게시글입니다.";
    private static final String DETAIL_COMMENT_TITLE = "댓글 내용";
    private static final String DETAIL_DELETED_COMMENT_CONTENT = "삭제된 댓글입니다.";
    private static final String DETAIL_REVIEW_TITLE = "리뷰 내용";
    private static final String DETAIL_DELETED_REVIEW_CONTENT = "삭제된 리뷰입니다.";

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

    @Override
    public Optional<AdminReportDetailResult> findReportDetail(Long reportId) {
        ReportJpaEntity requestedReport = entityManager.find(ReportJpaEntity.class, reportId);
        if (requestedReport == null) {
            return Optional.empty();
        }

        List<ReportJpaEntity> reports = entityManager.createQuery("""
                select r
                from ReportJpaEntity r
                where r.targetType = :targetType
                  and r.targetId = :targetId
                order by r.createdAt desc, r.id desc
                """, ReportJpaEntity.class)
                .setParameter("targetType", requestedReport.getTargetType())
                .setParameter("targetId", requestedReport.getTargetId())
                .getResultList();

        if (reports.isEmpty()) {
            return Optional.empty();
        }

        ReportJpaEntity latestReport = reports.get(0);
        DetailTargetContent targetContent = findDetailTargetContent(
                latestReport.getTargetType(),
                latestReport.getTargetId(),
                latestReport.getReportedMemberId()
        );
        MemberInfo reporter = findMemberInfo(latestReport.getReporterId());

        return Optional.of(new AdminReportDetailResult(
                latestReport.getId(),
                latestReport.getTargetType(),
                latestReport.getTargetId(),
                targetContent.title(),
                targetContent.content(),
                targetContent.url(),
                targetContent.authorId(),
                findMemberName(targetContent.authorId()),
                reports.size(),
                aggregateReasonCounts(reports),
                latestReport.getReporterId(),
                reporter.name(),
                reporter.username(),
                normalizeStatus(latestReport.getStatus()),
                latestReport.getMemo()
        ));
    }

    private List<AdminReportDetailResult.ReasonCount> aggregateReasonCounts(
            List<ReportJpaEntity> reports
    ) {
        Map<ReportType, Integer> counts = new EnumMap<>(ReportType.class);
        for (ReportJpaEntity report : reports) {
            if (report.getReportTypes() == null || report.getReportTypes().isBlank()) {
                continue;
            }
            for (String value : report.getReportTypes().split(",")) {
                try {
                    ReportType type = ReportType.valueOf(value.trim());
                    counts.merge(type, 1, Integer::sum);
                } catch (IllegalArgumentException ignored) {
                    // Unknown legacy values cannot be represented by ReportType.
                }
            }
        }

        List<AdminReportDetailResult.ReasonCount> result = new ArrayList<>();
        counts.forEach((reason, count) ->
                result.add(new AdminReportDetailResult.ReasonCount(reason, count)));
        result.sort(Comparator
                .comparingInt(AdminReportDetailResult.ReasonCount::count)
                .reversed()
                .thenComparingInt(reasonCount -> reasonCount.reason().ordinal()));
        return result;
    }

    private DetailTargetContent findDetailTargetContent(
            TargetType targetType,
            Long targetId,
            Long reportedMemberId
    ) {
        return switch (targetType) {
            case POST -> {
                PostJpaEntity post = entityManager.find(PostJpaEntity.class, targetId);
                if (post == null) {
                    yield new DetailTargetContent(
                            DETAIL_DELETED_POST_TITLE,
                            DETAIL_DELETED_POST_CONTENT,
                            null,
                            reportedMemberId
                    );
                }
                yield new DetailTargetContent(
                        post.getTitle(),
                        post.getStatus() == PostStatus.ACTIVE
                                ? post.getContent()
                                : DETAIL_DELETED_POST_CONTENT,
                        "/posts/" + post.getId(),
                        post.getAuthorId()
                );
            }
            case COMMENT -> {
                CommentJpaEntity comment = entityManager.find(CommentJpaEntity.class, targetId);
                if (comment == null) {
                    yield new DetailTargetContent(
                            DETAIL_COMMENT_TITLE,
                            DETAIL_DELETED_COMMENT_CONTENT,
                            null,
                            reportedMemberId
                    );
                }
                yield new DetailTargetContent(
                        DETAIL_COMMENT_TITLE,
                        comment.getStatus() == CommentStatus.ACTIVE
                                ? comment.getContent()
                                : DETAIL_DELETED_COMMENT_CONTENT,
                        "/posts/" + comment.getPostId(),
                        comment.getAuthorId()
                );
            }
            case REVIEW -> {
                ReviewJpaEntity review = entityManager.find(ReviewJpaEntity.class, targetId);
                if (review == null) {
                    yield new DetailTargetContent(
                            DETAIL_REVIEW_TITLE,
                            DETAIL_DELETED_REVIEW_CONTENT,
                            null,
                            reportedMemberId
                    );
                }
                yield new DetailTargetContent(
                        DETAIL_REVIEW_TITLE,
                        review.getStatus() == ReviewStatus.ACTIVE
                                ? review.getContent()
                                : DETAIL_DELETED_REVIEW_CONTENT,
                        "/courses/" + review.getCourseId(),
                        review.getMemberId()
                );
            }
        };
    }

    private MemberInfo findMemberInfo(Long memberId) {
        if (memberId == null) {
            return MemberInfo.EMPTY;
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(
                        "select name, username from members where member_id = :memberId")
                .setParameter("memberId", memberId)
                .setMaxResults(1)
                .getResultList();

        if (rows.isEmpty()) {
            return MemberInfo.EMPTY;
        }
        Object[] row = rows.get(0);
        return new MemberInfo(
                row[0] == null ? null : String.valueOf(row[0]),
                row[1] == null ? null : String.valueOf(row[1])
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
                yield post == null || post.getStatus() != PostStatus.ACTIVE
                        ? new TargetContent(DELETED_POST_TITLE, DELETED_POST_CONTENT)
                        : new TargetContent(post.getTitle(), post.getContent());
            }
            case COMMENT -> {
                CommentJpaEntity comment = entityManager.find(CommentJpaEntity.class, targetId);
                yield comment == null || comment.getStatus() != CommentStatus.ACTIVE
                        ? new TargetContent(COMMENT_TITLE, deletedCommentContent(comment))
                        : new TargetContent(COMMENT_TITLE, comment.getContent());
            }
            case REVIEW -> {
                ReviewJpaEntity review = entityManager.find(ReviewJpaEntity.class, targetId);
                yield review == null || review.getStatus() != ReviewStatus.ACTIVE
                        ? new TargetContent(REVIEW_TITLE, DELETED_REVIEW_CONTENT)
                        : new TargetContent(REVIEW_TITLE, review.getContent());
            }
        };
    }

    private String deletedCommentContent(CommentJpaEntity comment) {
        if (comment != null && comment.getStatus() == CommentStatus.ADMIN_DELETED) {
            return Comment.ADMIN_DELETED_MESSAGE;
        }
        return DELETED_COMMENT_CONTENT;
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

    private record DetailTargetContent(
            String title,
            String content,
            String url,
            Long authorId
    ) {
    }

    private record MemberInfo(
            String name,
            String username
    ) {
        private static final MemberInfo EMPTY = new MemberInfo(null, null);
    }
}
