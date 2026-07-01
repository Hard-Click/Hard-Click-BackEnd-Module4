package com.wanted.backend.domain.enrollment_management.infrastructure.persistence;

import com.wanted.backend.domain.community.application.port.EnrollmentCompletedCheckPort;
import com.wanted.backend.domain.enrollment_management.application.port.MyEnrolledCourseQueryPort;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentCompletedCheckAdapter implements EnrollmentCompletedCheckPort {

    private final MyEnrolledCourseQueryPort queryPort;

    public EnrollmentCompletedCheckAdapter(MyEnrolledCourseQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public boolean isCompleted(Long memberId, Long courseId) {
        return queryPort.findByMemberId(memberId).stream()
                .filter(data -> data.courseId().equals(courseId))
                .anyMatch(this::isCompletedCourse);
    }

    private boolean isCompletedCourse(MyEnrolledCourseQueryPort.MyEnrolledCourseData data) {
        return data.totalLessonCount() != null
                && data.totalLessonCount() > 0
                && data.totalLessonCount().equals(data.completedLessonCount());
    }
}