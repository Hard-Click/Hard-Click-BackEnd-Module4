package com.wanted.backend.domain.payment.application.port;

public interface CourseProgressRatePort {

    /**
     * 회원의 강의 진도율을 반환한다. (0.0 ~ 1.0)
     * 총 영상이 없으면 0.0 반환.
     */
    double getProgressRate(Long memberId, Long courseId);
}
