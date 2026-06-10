package com.wanted.backend.domain.enrollment_management.application.service;

import com.wanted.backend.domain.enrollment_management.application.port.MyEnrolledCourseQueryPort;
import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyCompletedCoursesUseCase;
import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyCompletedCourseService implements GetMyCompletedCoursesUseCase {

    private final MyEnrolledCourseQueryPort myEnrolledCourseQueryPort;

    @Override
    public List<MyCompletedCourseView> handle(Long memberId) {
        return myEnrolledCourseQueryPort.findByMemberId(memberId).stream()
                .filter(this::isCompletedCourse)
                .map(this::toView)
                .sorted(Comparator.comparing(
                        MyCompletedCourseView::completedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    private boolean isCompletedCourse(MyEnrolledCourseQueryPort.MyEnrolledCourseData data) {
        // enrollment status가 COMPLETED이거나, 전체 레슨을 모두 완료한 경우
        if (EnrollmentStatus.COMPLETED.equals(data.enrollmentStatus())) {
            return true;
        }
        return data.totalLessonCount() != null
                && data.totalLessonCount() > 0
                && data.totalLessonCount().equals(data.completedLessonCount());
    }

    private MyCompletedCourseView toView(MyEnrolledCourseQueryPort.MyEnrolledCourseData data) {
        return new MyCompletedCourseView(
                data.courseId(),
                data.courseTitle(),
                data.thumbnailUrl(),
                100,
                data.lastStudiedAt()
        );
    }
}
