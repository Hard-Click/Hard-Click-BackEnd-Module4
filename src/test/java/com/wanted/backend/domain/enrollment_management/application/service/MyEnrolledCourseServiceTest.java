package com.wanted.backend.domain.enrollment_management.application.service;

import com.wanted.backend.domain.enrollment_management.application.port.MyEnrolledCourseQueryPort;
import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyEnrolledCourseUseCase.MyEnrolledCourseView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MyEnrolledCourseServiceTest {

    private MyEnrolledCourseQueryPort queryPort;
    private MyEnrolledCourseService service;

    @BeforeEach
    void setUp() {
        queryPort = mock(MyEnrolledCourseQueryPort.class);
        service = new MyEnrolledCourseService(queryPort);
    }

    @Test
    void returnsMyEnrolledCoursesWithProgressRate() {
        LocalDateTime lastStudiedAt = LocalDateTime.of(2026, 5, 27, 10, 30);
        when(queryPort.findByMemberId(1L)).thenReturn(List.of(
                new MyEnrolledCourseQueryPort.MyEnrolledCourseData(
                        20L,
                        "Spring Course",
                        "https://example.com/thumb.png",
                        1,
                        4,
                        lastStudiedAt,
                        10L,
                        142
                )
        ));

        List<MyEnrolledCourseView> result = service.handle(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).courseId()).isEqualTo(20L);
        assertThat(result.get(0).courseTitle()).isEqualTo("Spring Course");
        assertThat(result.get(0).progressRate()).isEqualTo(25);
        assertThat(result.get(0).lastVideoId()).isEqualTo(10L);
        assertThat(result.get(0).lastPositionSeconds()).isEqualTo(142);
        assertThat(result.get(0).lastStudiedAt()).isEqualTo(lastStudiedAt);
    }

    @Test
    void sortsByLastStudiedAtDescAndPutsNeverStudiedCourseLast() {
        LocalDateTime older = LocalDateTime.of(2026, 5, 26, 10, 0);
        LocalDateTime newer = LocalDateTime.of(2026, 5, 27, 10, 0);
        when(queryPort.findByMemberId(1L)).thenReturn(List.of(
                data(1L, older),
                data(2L, null),
                data(3L, newer)
        ));

        List<MyEnrolledCourseView> result = service.handle(1L);

        assertThat(result).extracting(MyEnrolledCourseView::courseId)
                .containsExactly(3L, 1L, 2L);
    }

    @Test
    void returnsZeroProgressWhenCourseHasNoVideos() {
        when(queryPort.findByMemberId(1L)).thenReturn(List.of(
                new MyEnrolledCourseQueryPort.MyEnrolledCourseData(
                        20L,
                        "Empty Course",
                        null,
                        0,
                        0,
                        null,
                        null,
                        0
                )
        ));

        List<MyEnrolledCourseView> result = service.handle(1L);

        assertThat(result.get(0).progressRate()).isZero();
    }

    private MyEnrolledCourseQueryPort.MyEnrolledCourseData data(Long courseId, LocalDateTime lastStudiedAt) {
        return new MyEnrolledCourseQueryPort.MyEnrolledCourseData(
                courseId,
                "Course " + courseId,
                null,
                0,
                1,
                lastStudiedAt,
                courseId * 10,
                0
        );
    }
}
