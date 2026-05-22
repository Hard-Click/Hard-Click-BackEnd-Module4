package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.policy.VideoAccessPolicy;
import com.wanted.backend.domain.learning_activity.application.port.EnrollmentAccessPort;
import com.wanted.backend.domain.learning_activity.application.port.SubscriptionAccessPort;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VideoAccessServiceTest {

    private EnrollmentAccessPort enrollmentAccessPort;
    private SubscriptionAccessPort subscriptionAccessPort;
    private VideoAccessService service;

    @BeforeEach
    void setUp() {
        enrollmentAccessPort = mock(EnrollmentAccessPort.class);
        subscriptionAccessPort = mock(SubscriptionAccessPort.class);
        service = new VideoAccessService(
                enrollmentAccessPort,
                subscriptionAccessPort,
                new VideoAccessPolicy()
        );
    }

    @Test
    void throwsCourseNotPublishedWhenCourseIsNotPublished() {
        VideoAccessInfo accessInfo = accessInfo("DRAFT", 0, true);

        assertThatThrownBy(() -> service.validatePlayable(1L, accessInfo))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COURSE_NOT_PUBLISHED);

        verify(enrollmentAccessPort, never()).hasActiveEnrollment(1L, 20L);
        verify(subscriptionAccessPort, never()).hasActiveSubscription(1L);
    }

    @Test
    void passesWhenMemberHasActiveEnrollment() {
        VideoAccessInfo accessInfo = accessInfo("PUBLISHED", 10000, false);
        when(enrollmentAccessPort.hasActiveEnrollment(1L, 20L)).thenReturn(true);

        service.validatePlayable(1L, accessInfo);

        verify(enrollmentAccessPort).hasActiveEnrollment(1L, 20L);
        verify(subscriptionAccessPort).hasActiveSubscription(1L);
    }

    @Test
    void throwsEnrollmentRequiredWhenMemberHasNoAccess() {
        VideoAccessInfo accessInfo = accessInfo("PUBLISHED", 10000, false);
        when(enrollmentAccessPort.hasActiveEnrollment(1L, 20L)).thenReturn(false);
        when(subscriptionAccessPort.hasActiveSubscription(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.validatePlayable(1L, accessInfo))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENROLLMENT_REQUIRED);
    }

    private VideoAccessInfo accessInfo(String courseStatus, Integer coursePrice, Boolean preview) {
        return new VideoAccessInfo(
                10L,
                20L,
                courseStatus,
                coursePrice,
                preview,
                "https://stream.example.com/video.m3u8",
                300
        );
    }
}
