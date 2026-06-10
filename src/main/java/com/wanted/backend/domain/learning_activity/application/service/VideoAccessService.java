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

    /* comment.
    *   재생 가능 여부 검증 르흠을 담당한다.
    *   1. 강의가 공개 상태인지 확인
    *   2. 수강권 있는지 확인
    *   3. 구독권 있는지 확인
    *   4. policy 에게 최종 판단 맡김
    *   5. 실패하면 BusinessException 던짐
    * */

    private final EnrollmentAccessPort enrollmentAccessPort;
    private final SubscriptionAccessPort subscriptionAccessPort;
    private final VideoAccessPolicy videoAccessPolicy;

    // 1번
    public void validatePlayable(Long memberId, VideoAccessInfo accessInfo) {
        if (!accessInfo.isPublishedCourse()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_PUBLISHED);
        }

        // 2번
        boolean enrolled = enrollmentAccessPort.hasActiveEnrollment(memberId, accessInfo.courseId());
        // 3번
        boolean subscribed = subscriptionAccessPort.hasActiveSubscription(memberId);
        // 4번
        if (videoAccessPolicy.canPlay(accessInfo, enrolled, subscribed)) {
            return;
        }
        // 5번
        throw new BusinessException(ErrorCode.ENROLLMENT_REQUIRED);
    }
}
