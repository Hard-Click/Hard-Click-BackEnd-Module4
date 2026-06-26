package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.policy.VideoAccessPolicy;
import com.wanted.backend.domain.learning_activity.application.port.EnrollmentAccessPort;
import com.wanted.backend.domain.learning_activity.application.port.SubscriptionAccessPort;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VideoAccessServiceTest {

    private EnrollmentAccessPort enrollmentAccessPort;
    private SubscriptionAccessPort subscriptionAccessPort;
    private LearningActivityMetricRecorder metricRecorder;
    private VideoAccessService service;

    @BeforeEach
    void setUp() {
        enrollmentAccessPort = mock(EnrollmentAccessPort.class);
        subscriptionAccessPort = mock(SubscriptionAccessPort.class);
        metricRecorder = mock(LearningActivityMetricRecorder.class);
        service = new VideoAccessService(
                enrollmentAccessPort,
                subscriptionAccessPort,
                new VideoAccessPolicy(),
                metricRecorder
        );
    }

    @Test
    void 강의가_공개_상태가_아니면_예외가_발생한다() {
        VideoAccessInfo accessInfo = accessInfo("DRAFT", 0, true);

        assertThatThrownBy(() -> service.validatePlayable(1L, accessInfo))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COURSE_NOT_PUBLISHED);

        verify(enrollmentAccessPort, never()).hasActiveEnrollment(1L, 20L);
        verify(subscriptionAccessPort, never()).hasActiveSubscription(1L);
        verify(metricRecorder).recordResult(LearningActivityAction.VIDEO_ACCESS, "COURSE_NOT_PUBLISHED");
    }

    @Test
    void 회원이_활성_수강권을_가지면_검증을_통과한다() {
        VideoAccessInfo accessInfo = accessInfo("PUBLISHED", 10000, false);
        when(enrollmentAccessPort.hasActiveEnrollment(1L, 20L)).thenReturn(true);

        service.validatePlayable(1L, accessInfo);

        verify(enrollmentAccessPort).hasActiveEnrollment(1L, 20L);
        verify(subscriptionAccessPort).hasActiveSubscription(1L);
        verify(metricRecorder).recordResult(LearningActivityAction.VIDEO_ACCESS, null);
    }

    @Test
    void 메트릭_기록이_실패해도_검증_결과는_그대로_유지된다() {
        VideoAccessInfo accessInfo = accessInfo("PUBLISHED", 10000, false);
        when(enrollmentAccessPort.hasActiveEnrollment(1L, 20L)).thenReturn(true);
        doThrow(new RuntimeException("metric registry down"))
                .when(metricRecorder).recordResult(LearningActivityAction.VIDEO_ACCESS, null);

        assertThatCode(() -> service.validatePlayable(1L, accessInfo))
                .doesNotThrowAnyException();
    }

    @Test
    void 회원에게_접근권한이_없으면_예외가_발생한다() {
        VideoAccessInfo accessInfo = accessInfo("PUBLISHED", 10000, false);
        when(enrollmentAccessPort.hasActiveEnrollment(1L, 20L)).thenReturn(false);
        when(subscriptionAccessPort.hasActiveSubscription(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.validatePlayable(1L, accessInfo))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENROLLMENT_REQUIRED);

        verify(metricRecorder).recordResult(LearningActivityAction.VIDEO_ACCESS, "ENROLLMENT_REQUIRED");
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
