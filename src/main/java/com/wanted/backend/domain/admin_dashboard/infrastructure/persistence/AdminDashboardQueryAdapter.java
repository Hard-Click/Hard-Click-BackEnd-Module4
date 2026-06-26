package com.wanted.backend.domain.admin_dashboard.infrastructure.persistence;

import com.wanted.backend.domain.admin_dashboard.application.dto.AdminDashboardResult;
import com.wanted.backend.domain.admin_dashboard.application.port.AdminDashboardQueryPort;
import com.wanted.backend.domain.community.domain.model.PostStatus;
import com.wanted.backend.domain.community.domain.model.ReportStatus;
import com.wanted.backend.domain.community.domain.model.ReportType;
import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.community.infrastructure.persistence.PostJpaEntity;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.notice.domain.model.NoticeStatus;
import com.wanted.backend.domain.notice.infrastructure.persistence.NoticeJpaEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdminDashboardQueryAdapter implements AdminDashboardQueryPort {

    private static final int RECENT_ITEM_SIZE = 3;
    private static final int MIN_REPORT_COUNT = 3;

    private static final String GLOBAL_NOTICE_TYPE = "GLOBAL";
    private static final String DELETED_POST_TITLE = "삭제된 게시글";
    private static final String COMMENT_TITLE = "댓글 내용";
    private static final String REVIEW_TITLE = "리뷰 내용";

    private final EntityManager entityManager;

    @Override
    public AdminDashboardResult findDashboard() {
        return new AdminDashboardResult(
                countMembers(),
                countPendingReportGroups(),
                countCourses(),
                countNotices(),
                findRecentReports(),
                findRecentNotices()
        );
    }

    private long countMembers() {
        return entityManager.createQuery("""
                select count(member)
                from MemberJpaEntity member
                where member.status <> :withdrawn
                """, Long.class)
                .setParameter("withdrawn", MemberStatus.WITHDRAWN)
                .getSingleResult();
    }

    private long countPendingReportGroups() {
        Number count = (Number) entityManager.createNativeQuery("""
                select count(*)
                from reports latest
                where latest.status = 'PENDING'
                  and not exists (
                      select 1
                      from reports newer
                      where newer.target_type = latest.target_type
                        and newer.target_id = latest.target_id
                        and (
                            newer.created_at > latest.created_at
                            or (
                                newer.created_at = latest.created_at
                                and newer.report_id > latest.report_id
                            )
                        )
                  )
                  and (
                      select count(*)
                      from reports report
                      where report.target_type = latest.target_type
                        and report.target_id = latest.target_id
                  ) >= :minReportCount
                """)
                .setParameter("minReportCount", MIN_REPORT_COUNT)
                .getSingleResult();

        return count.longValue();
    }

    private long countCourses() {
        return entityManager.createQuery("""
                select count(course)
                from CourseJpaEntity course
                """, Long.class)
                .getSingleResult();
    }

    private long countNotices() {
        return entityManager.createQuery("""
                select count(notice)
                from NoticeJpaEntity notice
                where notice.type = :type
                  and notice.status = :status
                """, Long.class)
                .setParameter("type", GLOBAL_NOTICE_TYPE)
                .setParameter("status", NoticeStatus.PUBLISHED)
                .getSingleResult();
    }

    private List<AdminDashboardResult.RecentReport> findRecentReports() {
        return entityManager.createQuery("""
                select latest.id,
                       latest.targetType,
                       latest.targetId,
                       latest.status,
                       latest.reportTypes,
                       latest.createdAt
                from ReportJpaEntity latest, ReportJpaEntity report
                where report.targetType = latest.targetType
                  and report.targetId = latest.targetId
                  and not exists (
                      select newer.id
                      from ReportJpaEntity newer
                      where newer.targetType = latest.targetType
                        and newer.targetId = latest.targetId
                        and (
                            newer.createdAt > latest.createdAt
                            or (
                                newer.createdAt = latest.createdAt
                                and newer.id > latest.id
                            )
                        )
                  )
                group by latest.id,
                         latest.targetType,
                         latest.targetId,
                         latest.status,
                         latest.reportTypes,
                         latest.createdAt
                having count(report.id) >= :minReportCount
                order by latest.createdAt desc, latest.id desc
                """, Object[].class)
                .setParameter("minReportCount", (long) MIN_REPORT_COUNT)
                .setMaxResults(RECENT_ITEM_SIZE)
                .getResultList()
                .stream()
                .map(this::toRecentReport)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private AdminDashboardResult.RecentReport toRecentReport(Object[] row) {
        TargetType targetType = (TargetType) row[1];
        Long targetId = (Long) row[2];

        return new AdminDashboardResult.RecentReport(
                (Long) row[0],
                targetType,
                findTargetTitle(targetType, targetId),
                representativeReason((String) row[4]),
                normalizeStatus((ReportStatus) row[3]),
                (LocalDateTime) row[5]
        );
    }

    private List<AdminDashboardResult.RecentNotice> findRecentNotices() {
        return entityManager.createQuery("""
                select notice
                from NoticeJpaEntity notice
                where notice.type = :type
                  and notice.status = :status
                order by notice.createdAt desc, notice.id desc
                """, NoticeJpaEntity.class)
                .setParameter("type", GLOBAL_NOTICE_TYPE)
                .setParameter("status", NoticeStatus.PUBLISHED)
                .setMaxResults(RECENT_ITEM_SIZE)
                .getResultList()
                .stream()
                .map(notice -> new AdminDashboardResult.RecentNotice(
                        notice.getId(),
                        notice.getTitle(),
                        notice.isPinned(),
                        notice.getCreatedAt()
                ))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String findTargetTitle(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case POST -> {
                PostJpaEntity post = entityManager.find(PostJpaEntity.class, targetId);

                if (post == null || post.getStatus() != PostStatus.ACTIVE) {
                    yield DELETED_POST_TITLE;
                }

                yield post.getTitle();
            }
            case COMMENT -> COMMENT_TITLE;
            case REVIEW -> REVIEW_TITLE;
        };
    }

    private ReportType representativeReason(String reportTypes) {
        if (reportTypes == null || reportTypes.isBlank()) {
            return null;
        }

        try {
            return ReportType.valueOf(
                    reportTypes.split(",")[0].trim()
            );
        } catch (IllegalArgumentException exception) {
            return ReportType.OTHER;
        }
    }

    private ReportStatus normalizeStatus(ReportStatus status) {
        return status == null ? ReportStatus.PENDING : status;
    }
}
