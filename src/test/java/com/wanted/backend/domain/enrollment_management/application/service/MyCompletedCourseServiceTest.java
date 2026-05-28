package com.wanted.backend.domain.enrollment_management.application.service;

import com.wanted.backend.domain.enrollment_management.application.port.MyEnrolledCourseQueryPort;
import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyCompletedCoursesUseCase.MyCompletedCourseView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MyCompletedCourseServiceTest {

    private MyEnrolledCourseQueryPort queryPort;
    private MyCompletedCourseService service;

    @BeforeEach
    void setUp() {
        queryPort = mock(MyEnrolledCourseQueryPort.class);
        service = new MyCompletedCourseService(queryPort);
    }

    @Test
    void 모든_영상이_완료된_강의만_반환한다() {
        LocalDateTime completedAt = LocalDateTime.of(2026, 5, 28, 10, 0);
        when(queryPort.findByMemberId(1L)).thenReturn(List.of(
                data(10L, 3, 3, completedAt),
                data(20L, 1, 3, LocalDateTime.of(2026, 5, 27, 10, 0)),
                data(30L, 0, 0, null)
        ));

        List<MyCompletedCourseView> result = service.handle(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).courseId()).isEqualTo(10L);
        assertThat(result.get(0).progressRate()).isEqualTo(100);
        assertThat(result.get(0).completedAt()).isEqualTo(completedAt);
    }

    @Test
    void 완료일시_내림차순으로_정렬한다() {
        LocalDateTime older = LocalDateTime.of(2026, 5, 27, 10, 0);
        LocalDateTime newer = LocalDateTime.of(2026, 5, 28, 10, 0);
        when(queryPort.findByMemberId(1L)).thenReturn(List.of(
                data(10L, 1, 1, older),
                data(20L, 1, 1, newer)
        ));

        List<MyCompletedCourseView> result = service.handle(1L);

        assertThat(result).extracting(MyCompletedCourseView::courseId)
                .containsExactly(20L, 10L);
    }

    private MyEnrolledCourseQueryPort.MyEnrolledCourseData data(
            Long courseId,
            Integer completedLessonCount,
            Integer totalLessonCount,
            LocalDateTime lastStudiedAt
    ) {
        return new MyEnrolledCourseQueryPort.MyEnrolledCourseData(
                courseId,
                "Course " + courseId,
                null,
                completedLessonCount,
                totalLessonCount,
                lastStudiedAt,
                courseId * 10,
                0
        );
    }
}
