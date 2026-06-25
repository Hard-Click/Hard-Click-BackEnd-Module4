package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.GetCourseProgressCommand;
import com.wanted.backend.domain.learning_activity.application.port.CourseProgressQueryPort;
import com.wanted.backend.domain.learning_activity.application.port.EnrollmentAccessPort;
import com.wanted.backend.domain.learning_activity.application.usecase.GetCourseProgressUseCase;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCourseProgressService implements GetCourseProgressUseCase {

    private static final LearningActivityAction ACTION = LearningActivityAction.COURSE_PROGRESS;

    private final CourseProgressQueryPort courseProgressQueryPort;
    private final EnrollmentAccessPort enrollmentAccessPort;
    private final LearningActivityMetricRecorder metricRecorder;

    @Override
    public CourseProgressView handle(GetCourseProgressCommand command) {
        String errorCode = "UNKNOWN";
        try {
            if (!enrollmentAccessPort.hasActiveEnrollment(command.memberId(), command.courseId())) {
                throw new BusinessException(ErrorCode.ENROLLMENT_REQUIRED);
            }

            CourseProgressQueryPort.CourseProgressData progressData = courseProgressQueryPort
                    .findByMemberIdAndCourseId(command.memberId(), command.courseId());

            List<LessonProgressView> lessons = progressData.lessons().stream()
                    .map(lesson -> new LessonProgressView(
                            lesson.videoId(),
                            lesson.completed(),
                            lesson.lastPositionSeconds()
                    ))
                    .toList();

            int totalLessonCount = lessons.size();
            int completedLessonCount = (int) lessons.stream()
                    .filter(lesson -> Boolean.TRUE.equals(lesson.completed()))
                    .count();

            CourseProgressView view = new CourseProgressView(
                    progressData.courseId(),
                    calculateProgressRate(completedLessonCount, totalLessonCount),
                    completedLessonCount,
                    totalLessonCount,
                    lessons
            );

            errorCode = null;
            return view;
        } catch (BusinessException e) {
            errorCode = e.getErrorCode().name();
            throw e;
        } finally {
            recordMetric(errorCode);
        }
    }

    private void recordMetric(String errorCode) {
        if (errorCode == null) {
            metricRecorder.recordSuccess(ACTION);
        } else {
            metricRecorder.recordFailure(ACTION, errorCode);
        }
    }

    private BigDecimal calculateProgressRate(int completedLessonCount, int totalLessonCount) {
        if (totalLessonCount == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(completedLessonCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalLessonCount), 2, RoundingMode.HALF_UP);
    }
}
