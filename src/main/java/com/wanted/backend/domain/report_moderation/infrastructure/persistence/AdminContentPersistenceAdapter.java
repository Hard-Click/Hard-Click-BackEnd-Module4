package com.wanted.backend.domain.report_moderation.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.CommentStatus;
import com.wanted.backend.domain.community.domain.model.PostStatus;
import com.wanted.backend.domain.community.domain.model.ReviewStatus;
import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.community.infrastructure.persistence.CommentJpaEntity;
import com.wanted.backend.domain.community.infrastructure.persistence.PostJpaEntity;
import com.wanted.backend.domain.community.infrastructure.persistence.ReviewJpaEntity;
import com.wanted.backend.domain.report_moderation.application.command.AdminContentStatusCommand;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentResult;
import com.wanted.backend.domain.report_moderation.application.port.AdminContentCommandPort;
import com.wanted.backend.domain.report_moderation.application.port.AdminContentQueryPort;
import com.wanted.backend.domain.report_moderation.domain.model.AdminContentStatus;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminContentPersistenceAdapter implements AdminContentQueryPort, AdminContentCommandPort {

    private static final String DELETED_POST_CONTENT = "삭제된 게시글입니다.";
    private static final String ADMIN_DELETED_POST_CONTENT = "관리자에 의해 삭제된 게시글입니다.";
    private static final String DELETED_COMMENT_CONTENT = "삭제된 댓글입니다.";
    private static final String ADMIN_DELETED_COMMENT_CONTENT = "관리자에 의해 삭제된 댓글입니다.";
    private static final String DELETED_REVIEW_CONTENT = "삭제된 리뷰입니다.";
    private static final String ADMIN_DELETED_REVIEW_CONTENT = "관리자에 의해 삭제된 리뷰입니다.";

    private final EntityManager entityManager;

    @Override
    public Optional<AdminContentResult> findContent(TargetType contentType, Long contentId) {
        return switch (contentType) {
            case POST -> Optional.ofNullable(entityManager.find(PostJpaEntity.class, contentId))
                    .map(this::toPostResult);
            case COMMENT -> Optional.ofNullable(entityManager.find(CommentJpaEntity.class, contentId))
                    .map(this::toCommentResult);
            case REVIEW -> Optional.ofNullable(entityManager.find(ReviewJpaEntity.class, contentId))
                    .map(this::toReviewResult);
        };
    }

    @Override
    public AdminContentResult changeStatus(AdminContentStatusCommand command) {
        LocalDateTime now = LocalDateTime.now();

        return switch (command.contentType()) {
            case POST -> changePostStatus(command.contentId(), command.status(), now);
            case COMMENT -> changeCommentStatus(command.contentId(), command.status(), now);
            case REVIEW -> changeReviewStatus(command.contentId(), command.status(), now);
        };
    }

    private AdminContentResult changePostStatus(
            Long contentId,
            AdminContentStatus status,
            LocalDateTime now
    ) {
        PostJpaEntity post = findPost(contentId);

        if (status == AdminContentStatus.ADMIN_DELETED) {
            if (post.getStatus() == PostStatus.ADMIN_DELETED) {
                throw new BusinessException(ErrorCode.REPORT_TARGET_ALREADY_DELETED);
            }
            post.softDeleteByAdmin(now);
        }

        if (status == AdminContentStatus.ACTIVE && post.getStatus() != PostStatus.ACTIVE) {
            post.restoreByAdmin(now);
        }

        return toPostResult(post);
    }

    private AdminContentResult changeCommentStatus(
            Long contentId,
            AdminContentStatus status,
            LocalDateTime now
    ) {
        CommentJpaEntity comment = findComment(contentId);

        if (status == AdminContentStatus.ADMIN_DELETED) {
            if (comment.getStatus() == CommentStatus.ADMIN_DELETED) {
                throw new BusinessException(ErrorCode.REPORT_TARGET_ALREADY_DELETED);
            }
            comment.softDeleteByAdmin(now);
        }

        if (status == AdminContentStatus.ACTIVE && comment.getStatus() != CommentStatus.ACTIVE) {
            comment.restoreByAdmin(now);
        }

        return toCommentResult(comment);
    }

    private AdminContentResult changeReviewStatus(
            Long contentId,
            AdminContentStatus status,
            LocalDateTime now
    ) {
        ReviewJpaEntity review = findReview(contentId);

        if (status == AdminContentStatus.ADMIN_DELETED) {
            if (review.getStatus() == ReviewStatus.ADMIN_DELETED) {
                throw new BusinessException(ErrorCode.REPORT_TARGET_ALREADY_DELETED);
            }
            review.softDeleteByAdmin(now);
        }

        if (status == AdminContentStatus.ACTIVE && review.getStatus() != ReviewStatus.ACTIVE) {
            review.restoreByAdmin(now);
        }

        return toReviewResult(review);
    }

    private PostJpaEntity findPost(Long contentId) {
        PostJpaEntity post = entityManager.find(PostJpaEntity.class, contentId);
        if (post == null) {
            throw new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND);
        }
        return post;
    }

    private CommentJpaEntity findComment(Long contentId) {
        CommentJpaEntity comment = entityManager.find(CommentJpaEntity.class, contentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND);
        }
        return comment;
    }

    private ReviewJpaEntity findReview(Long contentId) {
        ReviewJpaEntity review = entityManager.find(ReviewJpaEntity.class, contentId);
        if (review == null) {
            throw new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND);
        }
        return review;
    }

    private AdminContentResult toPostResult(PostJpaEntity post) {
        return new AdminContentResult(
                TargetType.POST,
                post.getId(),
                post.getTitle(),
                postContent(post),
                post.getStatus().name(),
                post.getAuthorId(),
                findMemberName(post.getAuthorId()),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private AdminContentResult toCommentResult(CommentJpaEntity comment) {
        return new AdminContentResult(
                TargetType.COMMENT,
                comment.getId(),
                null,
                commentContent(comment),
                comment.getStatus().name(),
                comment.getAuthorId(),
                findMemberName(comment.getAuthorId()),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    private AdminContentResult toReviewResult(ReviewJpaEntity review) {
        return new AdminContentResult(
                TargetType.REVIEW,
                review.getId(),
                null,
                reviewContent(review),
                review.getStatus().name(),
                review.getMemberId(),
                findMemberName(review.getMemberId()),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    private String postContent(PostJpaEntity post) {
        if (post.getStatus() == PostStatus.ACTIVE) {
            return post.getContent();
        }

        if (post.getStatus() == PostStatus.ADMIN_DELETED) {
            return ADMIN_DELETED_POST_CONTENT;
        }

        return DELETED_POST_CONTENT;
    }

    private String commentContent(CommentJpaEntity comment) {
        if (comment.getStatus() == CommentStatus.ACTIVE) {
            return comment.getContent();
        }

        if (comment.getStatus() == CommentStatus.ADMIN_DELETED) {
            return ADMIN_DELETED_COMMENT_CONTENT;
        }

        return DELETED_COMMENT_CONTENT;
    }

    private String reviewContent(ReviewJpaEntity review) {
        if (review.getStatus() == ReviewStatus.ACTIVE) {
            return review.getContent();
        }

        if (review.getStatus() == ReviewStatus.ADMIN_DELETED) {
            return ADMIN_DELETED_REVIEW_CONTENT;
        }

        return DELETED_REVIEW_CONTENT;
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
}
