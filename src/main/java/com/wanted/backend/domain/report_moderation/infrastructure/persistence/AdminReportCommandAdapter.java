package com.wanted.backend.domain.report_moderation.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.CommentStatus;
import com.wanted.backend.domain.community.domain.model.PostStatus;
import com.wanted.backend.domain.community.domain.model.ReportStatus;
import com.wanted.backend.domain.community.domain.model.ReviewStatus;
import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.community.infrastructure.persistence.CommentJpaEntity;
import com.wanted.backend.domain.community.infrastructure.persistence.PostJpaEntity;
import com.wanted.backend.domain.community.infrastructure.persistence.ReportJpaEntity;
import com.wanted.backend.domain.community.infrastructure.persistence.ReviewJpaEntity;
import com.wanted.backend.domain.report_moderation.application.command.AdminReportDecisionCommand;
import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDecisionResult;
import com.wanted.backend.domain.report_moderation.application.port.AdminReportCommandPort;
import com.wanted.backend.domain.report_moderation.domain.model.AdminReportDecision;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminReportCommandAdapter implements AdminReportCommandPort {

    private final EntityManager entityManager;

    @Override
    public AdminReportDecisionResult decide(AdminReportDecisionCommand command) {
        ReportJpaEntity representativeReport = entityManager.find(
                ReportJpaEntity.class,
                command.reportId()
        );

        if (representativeReport == null) {
            throw new BusinessException(ErrorCode.REPORT_NOT_FOUND);
        }

        List<ReportJpaEntity> targetReports = findTargetReports(representativeReport);

        if (targetReports.isEmpty()) {
            throw new BusinessException(ErrorCode.REPORT_NOT_FOUND);
        }

        if (targetReports.stream().anyMatch(ReportJpaEntity::isProcessed)) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }

        validateRequest(command);

        boolean targetDeleted = false;

        if (command.decision() == AdminReportDecision.REJECT) {
            targetReports.forEach(report -> report.reject(command.memo()));
        }

        if (command.decision() == AdminReportDecision.DELETE) {
            deleteTarget(
                    representativeReport.getTargetType(),
                    representativeReport.getTargetId()
            );
            targetReports.forEach(report -> report.resolve(command.memo()));
            targetDeleted = true;
        }

        return new AdminReportDecisionResult(
                representativeReport.getId(),
                command.decision(),
                representativeReport.getStatus(),
                representativeReport.getMemo(),
                representativeReport.getTargetType(),
                representativeReport.getTargetId(),
                targetDeleted,
                representativeReport.getReportedMemberId()
        );
    }

    private List<ReportJpaEntity> findTargetReports(ReportJpaEntity report) {
        return entityManager.createQuery("""
                select r
                from ReportJpaEntity r
                where r.targetType = :targetType
                  and r.targetId = :targetId
                order by r.createdAt desc, r.id desc
                """, ReportJpaEntity.class)
                .setParameter("targetType", report.getTargetType())
                .setParameter("targetId", report.getTargetId())
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
    }

    private void validateRequest(AdminReportDecisionCommand command) {
        if (command.decision() == AdminReportDecision.REJECT && command.deleteTarget()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (command.decision() == AdminReportDecision.DELETE && !command.deleteTarget()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void deleteTarget(TargetType targetType, Long targetId) {
        LocalDateTime now = LocalDateTime.now();

        switch (targetType) {
            case POST -> deletePost(targetId, now);
            case COMMENT -> deleteComment(targetId, now);
            case REVIEW -> deleteReview(targetId, now);
        }
    }

    private void deletePost(Long targetId, LocalDateTime now) {
        PostJpaEntity post = entityManager.find(PostJpaEntity.class, targetId);

        if (post == null) {
            throw new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND);
        }

        if (post.getStatus() != PostStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.REPORT_TARGET_ALREADY_DELETED);
        }

        post.softDeleteByAdmin(now);
    }

    private void deleteComment(Long targetId, LocalDateTime now) {
        CommentJpaEntity comment = entityManager.find(CommentJpaEntity.class, targetId);

        if (comment == null) {
            throw new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND);
        }

        if (comment.getStatus() != CommentStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.REPORT_TARGET_ALREADY_DELETED);
        }

        comment.softDeleteByAdmin(now);
    }

    private void deleteReview(Long targetId, LocalDateTime now) {
        ReviewJpaEntity review = entityManager.find(ReviewJpaEntity.class, targetId);

        if (review == null) {
            throw new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND);
        }

        if (review.getStatus() != ReviewStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.REPORT_TARGET_ALREADY_DELETED);
        }

        review.softDeleteByAdmin(now);
    }
}
