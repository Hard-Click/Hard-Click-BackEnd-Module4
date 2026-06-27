package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.command.CreateReviewCommand;
import com.wanted.backend.domain.community.application.command.DeleteReviewCommand;
import com.wanted.backend.domain.community.application.command.UpdateReviewCommand;
import com.wanted.backend.domain.community.application.policy.CommunityAccessPolicy;
import com.wanted.backend.domain.community.application.policy.ReviewPolicy;
import com.wanted.backend.domain.community.application.usecase.ReviewCommandUseCase;
import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewCommandService implements ReviewCommandUseCase {

    private final ReviewRepository reviewRepository;
    private final ReviewPolicy reviewPolicy;
    private final CommunityAccessPolicy communityAccessPolicy;

    public ReviewCommandService(ReviewRepository reviewRepository,
                                ReviewPolicy reviewPolicy,
                                CommunityAccessPolicy communityAccessPolicy) {
        this.reviewRepository = reviewRepository;
        this.reviewPolicy = reviewPolicy;
        this.communityAccessPolicy = communityAccessPolicy;
    }

    @Override
    public Long handle(CreateReviewCommand command) {
        communityAccessPolicy.validateAccess(command.memberId());

        reviewPolicy.validateCompleted(command.memberId(), command.courseId());
        reviewPolicy.validateDuplicate(command.courseId(), command.memberId());

        Review saved = reviewRepository.save(
                Review.create(command.memberId(), command.courseId(), command.rating(), command.content())
        );

        return saved.getId();
    }

    @Override
    public Long update(UpdateReviewCommand command) {
        communityAccessPolicy.validateAccess(command.memberId());

        Review review = reviewRepository.findById(command.reviewId())
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        // 본인 리뷰 여부 확인 → Review 도메인이 판단
        if (!review.isOwner(command.memberId())) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_AUTHORIZED);
        }

        review.update(command.rating(), command.content());
        Review saved = reviewRepository.save(review);

        return saved.getId();
    }

    @Override
    public void delete(DeleteReviewCommand command) {
        communityAccessPolicy.validateAccess(command.memberId());

        Review review = reviewRepository.findById(command.reviewId())
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if (command.isAdmin()) {
            reviewRepository.adminDeleteById(command.reviewId());
            return;
        }

        if (!review.isOwner(command.memberId())) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_AUTHORIZED);
        }

        reviewRepository.deleteById(command.reviewId());
    }
}