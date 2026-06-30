package com.wanted.backend.domain.learning_activity.infrastructure.catalog;

import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.application.port.VideoPlayUrlPort;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoCatalogAdapter implements VideoCatalogPort {

    private final LessonReferenceRepository lessonRepository;
    private final CourseSectionReferenceRepository sectionRepository;
    private final CatalogCourseReferenceRepository courseRepository;
    private final VideoPlayUrlPort videoPlayUrlPort;

    @Override
    public Optional<VideoAccessInfo> findByVideoId(Long videoId) {
        return lessonRepository.findById(videoId)
                .flatMap(this::toDomain);
    }

    private Optional<VideoAccessInfo> toDomain(LessonReferenceEntity lesson) {
        return sectionRepository.findById(lesson.getSectionId())
                .flatMap(section -> courseRepository.findById(section.getCourseId())
                        .map(course -> toDomain(lesson, section, course)));
    }

    private VideoAccessInfo toDomain(
            LessonReferenceEntity lesson,
            CourseSectionReferenceEntity section,
            CatalogCourseReferenceEntity course
    ) {
        boolean isPreview = section.getOrderIndex() == 0 && lesson.getOrderIndex() == 0;

        // s3Key가 있으면 presigned GET URL을 실시간 발급, 없으면 null
        String streamingUrl = lesson.getS3Key() != null
                ? videoPlayUrlPort.generateUrl(lesson.getS3Key())
                : null;

        return new VideoAccessInfo(
                lesson.getId(),
                section.getCourseId(),
                course.getStatus(),
                course.getPrice(),
                isPreview,
                lesson.getS3Key(),
                streamingUrl,
                lesson.getDurationSeconds()
        );
    }
}
