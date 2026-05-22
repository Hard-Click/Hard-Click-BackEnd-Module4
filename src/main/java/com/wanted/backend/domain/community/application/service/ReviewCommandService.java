package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.command.CreateReviewCommand;
import com.wanted.backend.domain.community.application.policy.ReviewPolicy;
import com.wanted.backend.domain.community.application.usecase.ReviewCommandUseCase;
import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewCommandService implements ReviewCommandUseCase {

    private final ReviewRepository reviewRepository;
    private final ReviewPolicy reviewPolicy;

    public ReviewCommandService(ReviewRepository reviewRepository,
                                ReviewPolicy reviewPolicy) {
        this.reviewRepository = reviewRepository;
        this.reviewPolicy = reviewPolicy;
    }

    @Override
    public Long handle(CreateReviewCommand command) {
        reviewPolicy.validateDuplicate(command.courseId(), command.memberId());

        Review saved = reviewRepository.save(
                Review.create(command.memberId(), command.courseId(), command.rating(), command.content())
        );

        return saved.getId();
    }
}