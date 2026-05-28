package com.wanted.backend.domain.learning_activity.infrastructure.persistence;

import com.wanted.backend.domain.learning_activity.application.port.CourseProgressQueryPort;
import com.wanted.backend.domain.learning_activity.infrastructure.curriculum.CourseCurriculumReferenceEntity;
import com.wanted.backend.domain.learning_activity.infrastructure.curriculum.CourseCurriculumReferenceRepository;
import com.wanted.backend.domain.learning_activity.infrastructure.video.VideoReferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseProgressQueryAdapter implements CourseProgressQueryPort {

    private final CourseCurriculumReferenceRepository curriculumRepository;
    private final VideoReferenceRepository videoRepository;
    private final SpringDataVideoProgressRepository videoProgressRepository;

    @Override
    public CourseProgressData findByMemberIdAndCourseId(Long memberId, Long courseId) {
        List<Long> curriculumIds = curriculumRepository.findByCourseIdOrderByIdAsc(courseId).stream()
                .map(CourseCurriculumReferenceEntity::getId)
                .toList();

        Map<Long, VideoProgressJpaEntity> progressByVideoId = videoProgressRepository
                .findByMemberIdAndCourseId(memberId, courseId).stream()
                .collect(Collectors.toMap(VideoProgressJpaEntity::getVideoId, Function.identity()));

        List<LessonProgressData> lessons = videoRepository.findByCurriculumIdInOrderByIdAsc(curriculumIds).stream()
                .map(video -> {
                    VideoProgressJpaEntity progress = progressByVideoId.get(video.getId());
                    return new LessonProgressData(
                            video.getId(),
                            progress != null && Boolean.TRUE.equals(progress.getCompleted()),
                            progress == null ? 0 : progress.getLastPositionSec()
                    );
                })
                .toList();

        return new CourseProgressData(courseId, lessons);
    }
}
