package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.policy.VideoAccessPolicy;
import com.wanted.backend.domain.learning_activity.application.port.EnrollmentAccessPort;
import com.wanted.backend.domain.learning_activity.application.port.SubscriptionAccessPort;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoAccessService {

    private static final LearningActivityAction ACTION = LearningActivityAction.VIDEO_ACCESS;

    private final EnrollmentAccessPort enrollmentAccessPort;
    private final SubscriptionAccessPort subscriptionAccessPort;
    private final VideoAccessPolicy videoAccessPolicy;
    private final LearningActivityMetricRecorder metricRecorder;

    public void validatePlayable(Long memberId, VideoAccessInfo accessInfo) {
        String errorCode = "UNKNOWN";
        try {
            validatePublished(accessInfo);

            boolean enrolled = enrollmentAccessPort.hasActiveEnrollment(memberId, accessInfo.courseId());
            boolean subscribed = subscriptionAccessPort.hasActiveSubscription(memberId);

            if (!videoAccessPolicy.canPlay(accessInfo, enrolled, subscribed)) {
                throw new BusinessException(ErrorCode.ENROLLMENT_REQUIRED);
            }

            errorCode = null;
        } catch (BusinessException e) {
            errorCode = e.getErrorCode().name();
            throw e;
        } finally {
            recordMetric(errorCode);
        }
    }

    private void validatePublished(VideoAccessInfo accessInfo) {
        if (!accessInfo.isPublishedCourse()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_PUBLISHED);
        }
    }

    private void recordMetric(String errorCode) {
        if (errorCode == null) {
            metricRecorder.recordSuccess(ACTION);
        } else {
            metricRecorder.recordFailure(ACTION, errorCode);
        }
    }
}
