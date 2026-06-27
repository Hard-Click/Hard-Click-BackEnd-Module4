package com.wanted.backend.domain.learning_activity.infrastructure.persistence;

import com.wanted.backend.domain.learning_activity.application.port.CourseProgressQueryPort;
import com.wanted.backend.domain.learning_activity.infrastructure.catalog.CourseSectionReferenceEntity;
import com.wanted.backend.domain.learning_activity.infrastructure.catalog.CourseSectionReferenceRepository;
import com.wanted.backend.domain.learning_activity.infrastructure.catalog.LessonReferenceEntity;
import com.wanted.backend.domain.learning_activity.infrastructure.catalog.LessonReferenceRepository;
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

    private final CourseSectionReferenceRepository sectionRepository;
    private final LessonReferenceRepository lessonRepository;
    private final SpringDataVideoProgressRepository videoProgressRepository;

    @Override
    public CourseProgressData findByMemberIdAndCourseId(Long memberId, Long courseId) {
        // 영상 재생 식별자(videoId)는 이제 lesson.id다 — 섹션 순서 -> 레슨 순서로 정렬한다.
        List<CourseSectionReferenceEntity> sections = sectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<Long> sectionIds = sections.stream().map(CourseSectionReferenceEntity::getId).toList();

        Map<Long, List<LessonReferenceEntity>> lessonsBySectionId = lessonRepository
                .findBySectionIdInOrderByOrderIndexAsc(sectionIds).stream()
                .collect(Collectors.groupingBy(LessonReferenceEntity::getSectionId));

        Map<Long, VideoProgressJpaEntity> progressByVideoId = videoProgressRepository
                .findByMemberIdAndCourseId(memberId, courseId).stream()
                .collect(Collectors.toMap(VideoProgressJpaEntity::getVideoId, Function.identity()));

        List<LessonProgressData> lessons = sections.stream()
                .flatMap(section -> lessonsBySectionId.getOrDefault(section.getId(), List.of()).stream())
                .map(lesson -> {
                    VideoProgressJpaEntity progress = progressByVideoId.get(lesson.getId());
                    return new LessonProgressData(
                            lesson.getId(),
                            progress != null && Boolean.TRUE.equals(progress.getCompleted()),
                            progress == null ? 0 : progress.getLastPositionSec()
                    );
                })
                .toList();

        return new CourseProgressData(courseId, lessons);
    }
}
