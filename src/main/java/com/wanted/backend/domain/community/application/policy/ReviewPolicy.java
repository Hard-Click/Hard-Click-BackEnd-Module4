package com.wanted.backend.domain.community.application.policy;

import com.wanted.backend.domain.community.application.port.EnrollmentCompletedCheckPort;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ReviewPolicy {

    private final ReviewRepository reviewRepository;
    private final EnrollmentCompletedCheckPort enrollmentCompletedCheckPort;

    public ReviewPolicy(ReviewRepository reviewRepository,
                        EnrollmentCompletedCheckPort enrollmentCompletedCheckPort) {
        this.reviewRepository = reviewRepository;
        this.enrollmentCompletedCheckPort = enrollmentCompletedCheckPort;
    }

    public void validateDuplicate(Long courseId, Long memberId) {
        if (reviewRepository.existsByCourseIdAndMemberId(courseId, memberId)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
    }

    public void validateCompleted(Long memberId, Long courseId) {
        if (!enrollmentCompletedCheckPort.isCompleted(memberId, courseId)) {
            throw new BusinessException(ErrorCode.NOT_ENROLLED);
        }
    }
}