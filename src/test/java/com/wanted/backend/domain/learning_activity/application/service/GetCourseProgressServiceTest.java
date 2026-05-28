package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.GetCourseProgressCommand;
import com.wanted.backend.domain.learning_activity.application.port.CourseProgressQueryPort;
import com.wanted.backend.domain.learning_activity.application.port.EnrollmentAccessPort;
import com.wanted.backend.domain.learning_activity.application.usecase.GetCourseProgressUseCase.CourseProgressView;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetCourseProgressServiceTest {

    private CourseProgressQueryPort courseProgressQueryPort;
    private EnrollmentAccessPort enrollmentAccessPort;
    private GetCourseProgressService service;

    @BeforeEach
    void setUp() {
        courseProgressQueryPort = mock(CourseProgressQueryPort.class);
        enrollmentAccessPort = mock(EnrollmentAccessPort.class);
        service = new GetCourseProgressService(courseProgressQueryPort, enrollmentAccessPort);
    }

    @Test
    void returnsCourseProgress() {
        when(enrollmentAccessPort.hasActiveEnrollment(1L, 20L)).thenReturn(true);
        when(courseProgressQueryPort.findByMemberIdAndCourseId(1L, 20L))
                .thenReturn(new CourseProgressQueryPort.CourseProgressData(
                        20L,
                        List.of(
                                new CourseProgressQueryPort.LessonProgressData(10L, true, 300),
                                new CourseProgressQueryPort.LessonProgressData(11L, false, 42)
                        )
                ));

        CourseProgressView result = service.handle(new GetCourseProgressCommand(1L, 20L));

        assertThat(result.courseId()).isEqualTo(20L);
        assertThat(result.progressRate()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.completedLessonCount()).isEqualTo(1);
        assertThat(result.totalLessonCount()).isEqualTo(2);
        assertThat(result.lessons()).hasSize(2);
        assertThat(result.lessons().get(0).videoId()).isEqualTo(10L);
        assertThat(result.lessons().get(0).completed()).isTrue();
        assertThat(result.lessons().get(0).lastPositionSeconds()).isEqualTo(300);
    }

    @Test
    void returnsZeroProgressWhenCourseHasNoLessons() {
        when(enrollmentAccessPort.hasActiveEnrollment(1L, 20L)).thenReturn(true);
        when(courseProgressQueryPort.findByMemberIdAndCourseId(1L, 20L))
                .thenReturn(new CourseProgressQueryPort.CourseProgressData(20L, List.of()));

        CourseProgressView result = service.handle(new GetCourseProgressCommand(1L, 20L));

        assertThat(result.progressRate()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(result.completedLessonCount()).isZero();
        assertThat(result.totalLessonCount()).isZero();
        assertThat(result.lessons()).isEmpty();
    }

    @Test
    void throwsEnrollmentRequiredWhenEnrollmentDoesNotExist() {
        when(enrollmentAccessPort.hasActiveEnrollment(1L, 20L)).thenReturn(false);

        assertThatThrownBy(() -> service.handle(new GetCourseProgressCommand(1L, 20L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENROLLMENT_REQUIRED);
    }
}
