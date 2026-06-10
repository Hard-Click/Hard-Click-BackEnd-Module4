package com.wanted.backend.domain.enrollment_management.application.service;

import com.wanted.backend.domain.enrollment_management.application.port.MyEnrolledCourseQueryPort;
import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyEnrolledCourseUseCase;
import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyEnrolledCourseService implements GetMyEnrolledCourseUseCase {

    private final MyEnrolledCourseQueryPort myEnrolledCourseQueryPort;

    @Override
    public List<MyEnrolledCourseView> handle(Long memberId) {
        return myEnrolledCourseQueryPort.findByMemberId(memberId).stream()
                // 수강 완료(COMPLETED)는 별도 엔드포인트에서 조회 — 여기서는 제외
                .filter(data -> !EnrollmentStatus.COMPLETED.equals(data.enrollmentStatus()))
                .map(this::toView)
                // 최근 학습한 강의가 목록 상단에 오도록 정렬한다.
                .sorted(Comparator.comparing(
                        MyEnrolledCourseView::lastStudiedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    // 어댑터에서 조합한 조회 데이터를 화면 응답에 가까운 유스케이스 View로 변환한다.
    private MyEnrolledCourseView toView(MyEnrolledCourseQueryPort.MyEnrolledCourseData data) {
        return new MyEnrolledCourseView(
                data.courseId(),
                data.courseTitle(),
                data.thumbnailUrl(),
                calculateProgressRate(data.completedLessonCount(), data.totalLessonCount()),
                data.lastStudiedAt(),
                data.lastVideoId(),
                data.lastPositionSeconds()
        );
    }

    private Integer calculateProgressRate(Integer completedLessonCount, Integer totalLessonCount) {
        if (totalLessonCount == null || totalLessonCount == 0) {
            return 0;
        }
        int completed = completedLessonCount == null ? 0 : completedLessonCount;
        return (int) Math.round((completed * 100.0) / totalLessonCount);
    }
}
