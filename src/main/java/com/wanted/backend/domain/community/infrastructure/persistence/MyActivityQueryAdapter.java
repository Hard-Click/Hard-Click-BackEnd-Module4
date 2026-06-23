package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.application.port.MyActivityQueryPort;
import com.wanted.backend.domain.community.domain.model.PostStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyActivityQueryAdapter implements MyActivityQueryPort {

    private final SpringDataPostRepository postRepository;
    private final SpringDataCommentRepository commentRepository;
    private final SpringDataReviewRepository reviewRepository;

    @Override
    public MyActivityData findByMemberId(Long memberId) {
        return new MyActivityData(
                postRepository.findByAuthorIdAndStatusOrderByCreatedAtDesc(memberId, PostStatus.ACTIVE).stream()
                        .map(this::toPostActivity)
                        .toList(),
                commentRepository.findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(memberId).stream()
                        .map(this::toCommentActivity)
                        .toList(),
                reviewRepository.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
                        .map(this::toReviewActivity)
                        .toList()
        );
    }

    private MyPostActivityData toPostActivity(PostJpaEntity post) {
        return new MyPostActivityData(
                post.getId(),
                post.getBoardType(),
                post.getTitle(),
                post.getViewCount(),
                post.isAccepted(),
                post.getCreatedAt()
        );
    }

    private MyCommentActivityData toCommentActivity(CommentJpaEntity comment) {
        return new MyCommentActivityData(
                comment.getId(),
                comment.getPostId(),
                comment.getParentId(),
                comment.getContent(),
                comment.isAccepted(),
                comment.getCreatedAt()
        );
    }

    private MyReviewActivityData toReviewActivity(ReviewJpaEntity review) {
        return new MyReviewActivityData(
                review.getId(),
                review.getCourseId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
