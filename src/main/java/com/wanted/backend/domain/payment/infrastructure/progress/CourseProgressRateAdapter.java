package com.wanted.backend.domain.payment.infrastructure.progress;

import com.wanted.backend.domain.payment.application.port.CourseProgressRatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseProgressRateAdapter implements CourseProgressRatePort {

    private final CurriculumForRefundRepository curriculumRepository;
    private final VideoForRefundRepository videoRepository;
    private final VideoProgressForRefundRepository videoProgressRepository;

    @Override
    public double getProgressRate(Long memberId, Long courseId) {
        List<Long> curriculumIds = curriculumRepository.findByCourseId(courseId).stream()
                .map(CurriculumForRefundEntity::getId)
                .toList();

        if (curriculumIds.isEmpty()) {
            return 0.0;
        }

        long totalVideos = videoRepository.countByCurriculumIdIn(curriculumIds);
        if (totalVideos == 0) {
            return 0.0;
        }

        long completedVideos = videoProgressRepository.countByMemberIdAndCourseIdAndCompleted(memberId, courseId, true);
        return (double) completedVideos / totalVideos;
    }
}
