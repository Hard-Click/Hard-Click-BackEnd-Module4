package com.wanted.backend.domain.community.application.policy;

import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ReviewPolicy {

    private final ReviewRepository reviewRepository;

    public ReviewPolicy(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public void validateDuplicate(Long courseId, Long memberId) {
        if (reviewRepository.existsByCourseIdAndMemberId(courseId, memberId)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
    }
}